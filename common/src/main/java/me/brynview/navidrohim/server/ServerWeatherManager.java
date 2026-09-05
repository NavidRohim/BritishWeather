package me.brynview.navidrohim.server;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import com.google.gson.*;
import me.brynview.navidrohim.Util;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.server.locationsource.IPLocationSource;
import me.brynview.navidrohim.server.locationsource.LocationSource;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ServerWeatherManager
{
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

                @Nullable LocationSource.Location landmarkName = null;

                if (CommonClass.getWeatherManager().getState().isPresent())
                {
                    landmarkName = CommonClass.getWeatherManager().getState().get().location; // landmark (city) name will only be defined if user is using IP geolocating. Use "earth" by default.
                }

                return new WeatherState(WMOCode, weatherCondition, landmarkName);

            } catch (Exception e)
            {
                Constants.LOG.info("Got error when trying to deserialize WeatherState from server. Json following:\n\n{}", json.toString());
                throw e;
            }
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
        @Nullable LocationSource.Location location;

        public WeatherState(int WMOCode, WeatherCondition weatherCondition, @Nullable LocationSource.Location location)
        {
            this.WMOCode = WMOCode;
            this.weatherCondition = weatherCondition;
            this.location = location;
        }

        public String toString()
        {
            return "WeatherState[WMOCode=%s, weatherCondition=%s, location=%s]".formatted(WMOCode, weatherCondition.toString(), location);
        }

        public @Nullable LocationSource.Location getLocation()
        {
            return location;
        }

        public void setLocation(@Nullable LocationSource.Location location)
        {
            this.location = location;
        }

        @Nullable
        public static WeatherState deserialize(@Nullable JsonObject json)
        {
            try
            {
                int WMOCode = json.get("WMOCode").getAsInt();
                WeatherCondition weatherCondition = getConditionsFromWMOCode(WMOCode);
                String landmarkName = json.get("landmarkName").getAsString();
                float lat = json.get("lat").getAsFloat();
                float lon = json.get("lon").getAsFloat();

                return new WeatherState(WMOCode, weatherCondition, new LocationSource.Location(lat, lon, landmarkName));
            } catch (NullPointerException e)
            {
                return null;
            }
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

    private static final List<Integer> LIGHT_HAIL = List.of(87, 89, 93, 96);
    private static final List<Integer> HEAVY_HAIL = List.of(88, 90, 94, 99);

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(WeatherState.class, new WeatherResponseDecoder()).create();

    @Nullable private WeatherState STATE = null;
    private final Consumer<MinecraftServer> WEATHER_CHANGE_CALLBACK;

    private void changeServerWeather(MinecraftServer minecraftServer)
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
            default -> {
                if (STATE.weatherCondition.isHail())
                {
                    minecraftServer.setWeatherParameters(0, 9999, true, false);
                }
            }
        }
    }

    private static WeatherCondition getConditionsFromWMOCode(int code)
    {
        WeatherCondition conditions = WeatherCondition.CLOUDY;
        // src: https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
        if (LIGHT_HAIL.contains(code))
        {
            conditions = WeatherCondition.HAIL_STAGE_3;
        } else if (HEAVY_HAIL.contains(code))
        {
            conditions = WeatherCondition.HAIL_STAGE_1;
        }
        else if (code == 17) // Thunder, no rain
        {
            conditions = WeatherCondition.THUNDERSTORM_NO_RAIN;
        }
        else if (code >= 50 && code <= 94) // General precipitation. Generalised into just "RAINY" for minecraft purposes
        {
            conditions = WeatherCondition.RAINY;
        }
        else if (code >= 95 && code <= 99) // Thunderstorm with rain.
        {
            conditions = WeatherCondition.THUNDERSTORM;
        }

        return conditions;
    }

    private static boolean isPlayerSafeFromHail(ServerPlayer player)
    {
        BlockPos headHeight = player.blockPosition().above(2);
        int heightBlockAtY = player.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, headHeight).getY();
        return heightBlockAtY >= headHeight.getY() || player.hasItemInSlot(EquipmentSlot.HEAD);
    }

    public void tick(MinecraftServer server)
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

    public ServerWeatherManager(Consumer<MinecraftServer> weatherChangeCallback)
    {
        this.WEATHER_CHANGE_CALLBACK = weatherChangeCallback;
    }

    public Optional<WeatherState> getState()
    {
        return Optional.ofNullable(STATE);
    }
    /*
    Get new weather state. This is called every 120 seconds by default as specified in config.
     */
    private void fetchNewWeatherState(MinecraftServer server, LocationSource.Location location, LocationSource locationSource)
    {
        Constants.LOG.info("Fetching weather for {} {}", location, STATE);

        // Make request to open-mateo. No API key needed for our purposes.
        URI uriEndpoint = Util.getWeatherAPIUrl(location);
        HttpRequest request = HttpRequest.newBuilder().uri(uriEndpoint).GET().build();

        // Send async, check for correct status etc.
        // https://open-meteo.com/en/docs useful for figuring out API endpoint
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse ->
        {
            int statusCode = stringHttpResponse.statusCode();

            if (statusCode == 200)
            {
                WeatherState weatherState = GSON.fromJson(stringHttpResponse.body(), WeatherState.class);
                setStateAndLocation(weatherState, location);

                Constants.LOG.debug("Fetching weather state for {}", weatherState + uriEndpoint.toString());
                if (!weatherState.equals(STATE)) // Check if weather has actually changed, if not, just ignore and carry on.
                {
                    changeServerWeather(server);

                    // Again, only cache state if using IP.
                    if (locationSource instanceof IPLocationSource)
                    {
                        CommonClass.getCache().setCachedWeatherState(Constants.USER_IP, STATE);
                    }

                    WEATHER_CHANGE_CALLBACK.accept(server);
                    Constants.LOG.info("Sent weather change packets to clients");
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

    public void setState(WeatherState weatherState)
    {
        STATE = weatherState;
    }

    public void setStateAndLocation(WeatherState weatherState, LocationSource.Location locationSource)
    {
        STATE = weatherState;
        STATE.setLocation(locationSource);
    }

    /*
    Get clients coordinates. This is called once when the user loads into the main menu of minecraft for the first time.
     */
    public void setWeather(MinecraftServer server)
    {
        CommonClass.getConfig().getLocationSource().getLocation((loc, locationSource) -> fetchNewWeatherState(server, loc, locationSource));
    }
}
