package me.brynview.navidrohim.server;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import com.google.gson.*;
import me.brynview.navidrohim.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherManager
{
    // 999 are default values to check if these values have been changed by IP geolocating.
    private static float lat_ip = 999;
    private static float lon_ip = 999;
    private static String area = "Earth";

    private static WeatherCondition getConditionsFromWMOCode(int code)
    {
        WeatherCondition conditions = WeatherCondition.CLOUDY;

        // src: https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
        if (code == 17) // Thunder, no rain
        {
            conditions = WeatherCondition.THUNDERSTORM_NO_RAIN;
        }
        else if (code >= 50 && code <= 94) // General precipitation. Generalised into just "RAINY" for minecraft purposes
        {
            conditions = WeatherCondition.RAINY;
        } else if (code >= 95 && code <= 99) // Thunderstorm with rain.
        {
            conditions = WeatherCondition.THUNDERSTORM;
        }

        return conditions;
    }

    public static @Nullable WeatherState getState()
    {
        return STATE;
    }

    private static Identifier getHailTexture(int intensity)
    {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/environment/hail%s.png".formatted(intensity));
    }

    private static boolean isPlayerSafeFromHail(ServerPlayer player)
    {
        BlockPos headHeight = player.blockPosition().above(2);
        int heightBlockAtY = player.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, headHeight).getY();
        return heightBlockAtY >= headHeight.getY() || player.hasItemInSlot(EquipmentSlot.HEAD);
    }

    public static void tick(MinecraftServer server)
    {
        DamageSource hailDamage = new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageSources.HAIL_DAMAGE));

        server.getPlayerList().getPlayers().forEach(player ->
                {
                    if (!isPlayerSafeFromHail(player))
                    {
                        player.hurtServer(player.level(), hailDamage, 0.5f / STATE.getWeatherCondition().getHailLevel());
                    }
                });
    }

    // Deserializer class for getting response from open-mateo endpoint
    private static class WeatherResponseDecoder implements JsonDeserializer<WeatherState>
    {
        // Only deserialize
        @Override
        public WeatherState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            try
            {
                int WMOCode = json.getAsJsonObject().getAsJsonObject("current").get("weather_code").getAsInt();
                WeatherCondition weatherCondition = getConditionsFromWMOCode(WMOCode);

                String landmarkName = STATE != null ? STATE.landmarkName : area; // landmark (city) name will only be defined if user is using IP geolocating. Use "earth" by default.
                return new WeatherState(WMOCode, weatherCondition, landmarkName, getLatFromIP(), getLonFromIP());

            } catch (Exception e)
            {
                Constants.LOG.info("Got error when trying to deserialize WeatherState from server. Json following:\n\n{}", json.toString());
                throw e;
            }
        }
    }

    public enum WeatherCondition
    {
        CLOUDY,
        RAINY,
        THUNDERSTORM,
        THUNDERSTORM_NO_RAIN,

        HAIL_STAGE_1(1),
        HAIL_STAGE_2(2),
        HAIL_STAGE_3(3);

        @Nullable
        final Identifier weatherTexture;
        final int hailLevel;

        WeatherCondition(int hailLevel)
        {
            this.hailLevel = hailLevel;
            this.weatherTexture = getHailTexture(hailLevel);
        }

        WeatherCondition()
        {
            this.hailLevel = -1;
            this.weatherTexture = null;
        }

        public boolean isHail()
        {
            return hailLevel > 0;
        }

        public int getHailLevel()
        {
            return hailLevel;
        }

        public @Nullable Identifier getWeatherTexture()
        {
            return weatherTexture;
        }
    }

    /*
    Stores current weather state for location. Includes lat/lon, nearest city, WMO Code, and weather condition enum for convenience.
    View WMO Codes here: https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
     */
    public static class WeatherState
    {
        int WMOCode;
        transient WeatherCondition weatherCondition;
        String landmarkName;
        float lat;
        float lon;

        public WeatherState(int WMOCode, WeatherCondition weatherCondition, String landmarkName, float lat, float lon)
        {
            this.WMOCode = WMOCode;
            this.weatherCondition = weatherCondition;
            this.landmarkName = landmarkName;
            this.lat = lat;
            this.lon = lon;
        }

        public String toString()
        {
            return "WeatherState[WMOCode=%s, weatherCondition=%s, landmarkName=%s, lat=%f, lon=%f]".formatted(WMOCode, weatherCondition.toString(), landmarkName, lat, lon);
        }

        public String serialize()
        {
            return GSON.toJson(this);
        }

        @Nullable
        public static WeatherState deserialize(@Nullable String json)
        {
            if (json == null || json.isEmpty() || json.equals("{}"))
            {
                return null;
            }
            JsonObject rawWeatherStateData = JsonParser.parseString(json).getAsJsonObject();
            int WMOCode = rawWeatherStateData.get("WMOCode").getAsInt();
            WeatherCondition weatherCondition = getConditionsFromWMOCode(WMOCode);
            String landmarkName = rawWeatherStateData.get("landmarkName").getAsString();
            float lat = rawWeatherStateData.get("lat").getAsFloat();
            float lon = rawWeatherStateData.get("lon").getAsFloat();

            return new WeatherState(WMOCode, weatherCondition, landmarkName, lat, lon);
        }

        public WeatherCondition getWeatherCondition()
        {
            return weatherCondition;
        }

        /*
                Used to see if the weather state has changed from last check
                 */
        public boolean equals(@Nullable Object o)
        {
            if (o instanceof WeatherState)
            {
                return ((WeatherState) o).weatherCondition == weatherCondition;
            }
            return false;
        }
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(WeatherState.class, new WeatherResponseDecoder()).create();
    @Nullable private static WeatherState STATE = null;

    private static void changeServerWeather(MinecraftServer minecraftServer)
    {
        Constants.LOG.info("Changing weather state to " + STATE);

        // THUNDERSTORM_NO_RAIN may not do anything. I have never seen there be no rain but thunder in minecraft.
        if (STATE == null)
        {
            Constants.LOG.warn("Trying to change weather in-game before weather state has been established. Call WeatherManager.fetchNewWeatherState() first.");
        }

        // look into ServerLevel and ClientLevel for possible new weather states.

        switch (STATE.weatherCondition)
        {
            case CLOUDY -> minecraftServer.setWeatherParameters(9999, 0, false, false);
            case RAINY -> minecraftServer.setWeatherParameters(0, 9999, true, false);
            case THUNDERSTORM -> minecraftServer.setWeatherParameters(0, 9999, true, true);
            case THUNDERSTORM_NO_RAIN -> minecraftServer.setWeatherParameters(0, 0, false, true);
        }
    }

    /*
    Gets Latitude determined from setCoordinatesFromIP.
    If usePlayerIP in config is `false`, it will use the one determined in the config by the user.
     */
    public static float getLatFromIP()
    {
        return CommonClass.getConfig().usePlayerIP() && lat_ip < 999 ? lat_ip : CommonClass.getConfig().getLatitude();
    }

    /*
    Gets Longitude determined from setCoordinatesFromIP.
    If usePlayerIP in config is `false`, it will use the one determined in the config by the user.
     */
    public static float getLonFromIP()
    {
        return CommonClass.getConfig().usePlayerIP() && lon_ip < 999 ? lon_ip : CommonClass.getConfig().getLongitude();

    }

    /*
    Get new weather state. This is called every 120 seconds by default as specified in config.
     */
    private static void fetchNewWeatherState(MinecraftServer server)
    {
        Constants.LOG.info("Fetching weather for {} {} {}", getLatFromIP(), getLonFromIP(), STATE);

        // Make request to open-mateo. No API key needed for our purposes.
        HttpRequest request = HttpRequest.newBuilder().uri(Util.getWeatherAPIUrl()).GET().build();

        // Send async, check for correct status etc.
        // https://open-meteo.com/en/docs useful for figuring out API endpoint
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse ->
        {
            int statusCode = stringHttpResponse.statusCode();

            if (statusCode == 200)
            {
                WeatherState weatherState = GSON.fromJson(stringHttpResponse.body(), WeatherState.class);
                Constants.LOG.debug("Fetching weather state for {}", weatherState + Util.getWeatherAPIUrl().toString());
                if (!weatherState.equals(STATE)) // Check if weather has actually changed, if not, just ignore and carry on.
                {
                    setState(weatherState);
                    changeServerWeather(server);

                    // Again, only cache state if using IP.
                    if (CommonClass.getConfig().usePlayerIP())
                    {
                        CommonClass.CACHE.setCachedWeatherState(Constants.USER_IP, STATE);
                    }
                }

            } else { // Error response
                String reason = stringHttpResponse.body();
                JsonObject errorObj = JsonParser.parseString(reason).getAsJsonObject();
                if (errorObj.has("error") && errorObj.get("error").getAsBoolean())
                {
                    reason = errorObj.get("reason").getAsString();
                }
                Constants.LOG.warn("Got {} status code while trying to fetch WeatherState. Reason: {}",  statusCode, reason);
                Constants.LOG.debug(stringHttpResponse.body());
            }

        }).exceptionally(exc -> {
                if (exc.getCause() instanceof ConnectException) // No internet connection
                {
                    Constants.LOG.debug("No internet connection. Cannot fetch weather.");
                } else
                {
                    Constants.LOG.error("Uncatchable error while fetching the weather: ", exc);
                }
            return null;
            }
        );
    }

    public static void setState(WeatherState weatherState)
    {
        STATE = CommonClass.DEBUG_MASTER_WEATHER_STATE;
    }

    /*
    Get clients coordinates. This is called once when the user loads into the main menu of minecraft for the first time.
     */
    public static void setWeather(MinecraftServer server)
    {
        if (CommonClass.getConfig().usePlayerIP())
        {
            if (CommonClass.CACHE.isIpCached())
            {
                Constants.LOG.debug("Cache hit for weather state -> {}", STATE);
                WeatherManager.fetchNewWeatherState(server);
                return;
            }

            Constants.LOG.info("IP not cached. Fetching from server IP API");
            // Make API request
            HttpRequest requestIPApi = HttpRequest.newBuilder(Constants.IP_API_ENDPOINT).GET().build();

            // Send request asynchronously. Check status code is valid and API request is successful. View docs here
            // https://ip-api.com/docs/api:json
            HTTP_CLIENT.sendAsync(requestIPApi, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> {

                int statusCode =            stringHttpResponse.statusCode();
                JsonObject returnedObj =    JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject();
                // TODO error handling
                if (statusCode == 200 && returnedObj.get("status").getAsString().equals("success"))
                {
                    lat_ip =        returnedObj.get("lat").getAsFloat();
                    lon_ip =        returnedObj.get("lon").getAsFloat();
                    area =          returnedObj.get("city").getAsString() + returnedObj.get("district").getAsString(); // "district" may be irrelevant in the UK?
                    if (STATE != null)
                    {
                        STATE.landmarkName = area;
                    }

                    WeatherManager.fetchNewWeatherState(server);
                }
            });
        } else {
            WeatherManager.fetchNewWeatherState(server);
        }
    }
}
