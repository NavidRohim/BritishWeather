package me.brynview.navidrohim.server.weather;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import com.google.gson.*;
import me.brynview.navidrohim.Util;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.server.DamageSources;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
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
                return new WeatherState(WMOCode, weatherCondition, BritishWeather.getWeatherManager().getState().location); // landmark (city) name will only be defined if user is using IP geolocating. Use "earth" by default.

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
        @Nullable WeatherLocationSource.Location location;

        public WeatherState(int WMOCode, WeatherCondition weatherCondition, @Nullable WeatherLocationSource.Location location)
        {
            this.WMOCode = WMOCode;
            this.weatherCondition = weatherCondition;
            this.location = location;
        }

        public WeatherState(int wmoCode, String name, float latitude, float longitude)
        {
            this.WMOCode = wmoCode;
            this.weatherCondition = getConditionsFromWMOCode(wmoCode);
            this.location = new WeatherLocationSource.Location(longitude, latitude, name);
        }

        public String toString()
        {
            return "WeatherState[WMOCode=%s, weatherCondition=%s, location=%s]".formatted(WMOCode, weatherCondition.toString(), location);
        }

        public boolean isEmpty()
        {
            return WMOCode == -1;
        }

        public @Nullable WeatherLocationSource.Location getLocation()
        {
            return location;
        }

        public void setLocation(@Nullable WeatherLocationSource.Location location)
        {
            this.location = location;
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

        public static WeatherState empty()
        {
            return new WeatherState(-1, WeatherCondition.DEFAULT, null);
        }
    }

    private static final List<Integer> LIGHT_HAIL = List.of(87, 89, 93, 96);
    private static final List<Integer> HEAVY_HAIL = List.of(88, 90, 94, 99);

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(WeatherState.class, new WeatherResponseDecoder()).create();

    @NotNull
    private volatile WeatherState STATE = WeatherState.empty();
    private final Consumer<MinecraftServer> WEATHER_CHANGE_CALLBACK;

    private void changeServerWeather(MinecraftServer minecraftServer)
    {
        Constants.LOG.info("Changing server weather state to {}", STATE.weatherCondition);

        // THUNDERSTORM_NO_RAIN may not do anything. I have never seen there be no rain but thunder in minecraft.
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
                } else {
                    Constants.LOG.error("Invalid weather condition: {}",  STATE.weatherCondition);
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

    private static boolean isNotSafeFromHail(LivingEntity entity)
    {
        return isInRain(entity) && entity.level().canSeeSky(entity.blockPosition()) && !entity.hasItemInSlot(EquipmentSlot.HEAD);
    }

    private static boolean isInRain(LivingEntity entity)
    {
        BlockPos pos = entity.blockPosition();
        return entity.level().isRainingAt(pos) || entity.level().isRainingAt(BlockPos.containing(pos.getX(), entity.getBoundingBox().maxY, pos.getZ()));
    }

    public void tick(MinecraftServer server, long tick)
    {
        if ( tick % 45 == 0 && STATE.getWeatherCondition().isHail() && STATE.weatherCondition.getHailLevel() == 1 )
        {
            DamageSource hailDamage = new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageSources.HAIL_DAMAGE));
            float damageToTake = 0.5f;

            server.getPlayerList().getPlayers().forEach(player ->
            {
                ServerLevel level = player.level();
                BlockPos playerBlockPos = player.blockPosition();
                AABB searchArea = AABB.encapsulatingFullBlocks(playerBlockPos, playerBlockPos.atY((int) (player.getY() + 40))).inflate(16);

                // Hurt surrounding entities
                level.getEntitiesOfClass(LivingEntity.class, searchArea, ServerWeatherManager::isNotSafeFromHail).forEach(entity -> {
                    if (entity.getRandom().nextInt() % 2 == 0)
                    {
                        entity.hurtServer(level, hailDamage, damageToTake);
                    }
                });

                // Hurt player
                if (isNotSafeFromHail(player))
                {
                    player.hurtServer(level, hailDamage, damageToTake);
                }
            });
        }
    }

    public ServerWeatherManager(Consumer<MinecraftServer> weatherChangeCallback)
    {
        this.WEATHER_CHANGE_CALLBACK = weatherChangeCallback;
    }

    public Consumer<MinecraftServer> getWeatherChangeCallback()
    {
        return WEATHER_CHANGE_CALLBACK;
    }

    public WeatherState getState()
    {
        return STATE;
    }
    /*
    Get new weather state. This is called every 120 seconds by default as specified in config.
     */
    private void fetchWeatherState(MinecraftServer server, WeatherLocationSource.Location location, WeatherLocationSource weatherLocationSource, String cacheKey)
    {
        Constants.LOG.info("Fetching weather for {}", location);
        Constants.LOG.info("Current STATE is {}", STATE);

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
                Constants.LOG.debug("New STATE is {}", weatherState);

                if (!weatherState.equals(STATE)) // Check if weather has actually changed, if not, just ignore and carry on.
                {
                    setStateAndLocation(weatherState);
                    WEATHER_CHANGE_CALLBACK.accept(server);

                    Constants.LOG.info("Sent weather change packets to clients");
                }
                STATE.setLocation(location);
                changeServerWeather(server);
                weatherLocationSource.setCachedWeatherState(cacheKey, STATE);

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

    public void setStateAndLocation(WeatherState weatherState)
    {
        STATE = weatherState;
    }

    /*
    Get clients coordinates. This is called once when the user loads into the main menu of minecraft for the first time.
     */
    public void setWeather(MinecraftServer server)
    {
        BritishWeather.getConfig().getLocationSource().getLocation((loc, locationSource, key) -> fetchWeatherState(server, loc, locationSource, key));
    }
}
