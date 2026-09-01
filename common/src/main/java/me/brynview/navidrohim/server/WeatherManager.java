package me.brynview.navidrohim.server;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import com.google.gson.*;
import me.brynview.navidrohim.Util;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherManager
{
    // 999 are default values to check if these values have been changed by IP geolocating.
    private static float lat_ip = 999;
    private static float lon_ip = 999;
    private static String area = "Earth";

    // Deserializer class for getting response from open-mateo endpoint
    private static class WeatherResponseDecoder implements JsonDeserializer<WeatherState>
    {
        // Only deserialise
        @Override
        public WeatherState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            try
            {
                int WMOCode = json.getAsJsonObject().getAsJsonObject("current").get("weather_code").getAsInt();
                WeatherCondition conditions = WeatherCondition.CLOUDY;

                // src: https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
                if (WMOCode == 17) // Thunder, no rain
                {
                    conditions = WeatherCondition.THUNDERSTORM_NO_RAIN;
                }
                else if (WMOCode >= 50 && WMOCode <= 94) // General precipitation. Generalised into just "RAINY" for minecraft purposes
                {
                    conditions = WeatherCondition.RAINY;
                } else if (WMOCode >= 95 && WMOCode <= 99) // Thunderstorm with rain.
                {
                    conditions = WeatherCondition.THUNDERSTORM;
                }
                // If no condition is met, will be cloudy.
                String landmarkName = STATE != null ? STATE.landmarkName : area; // landmark (city) name will only be defined if user is using IP geolocating. Use "earth" by default.
                return new WeatherState(WMOCode, conditions, landmarkName, getLatFromIP(), getLonFromIP());

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
        THUNDERSTORM_NO_RAIN
    }

    /*
    Stores current weather state for location. Includes lat/lon, nearest city, WMO Code, and weather condition enum for convenience.
    View WMO Codes here: https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
     */
    public static class WeatherState
    {
        int WMOCode;
        WeatherCondition weatherCondition;
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

    private static void onWeatherChanged(WeatherManager.WeatherState weatherState, MinecraftServer minecraftServer)
    {
        Constants.LOG.info("Changing weather state to " + weatherState);

        // THUNDERSTORM_NO_RAIN may not do anything. I have never seen there be no rain but thunder in minecraft.
        switch (weatherState.weatherCondition)
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
    public static void fetchNewWeatherState(MinecraftServer server)
    {
        Constants.LOG.info("Fetching weather for {} {}", getLatFromIP(), getLonFromIP());

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
                    STATE = weatherState;
                    onWeatherChanged(STATE, server);
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
                Constants.LOG.error("Error while fetching the weather: ", exc);
                return null;
            }
        );
    }

    /*
    Get clients coordinates. This is called once when the user loads into the main menu of minecraft for the first time.
     */
    public static void setCoordinatesFromIP()
    {
        @Nullable URI IPAPIURI = Util.getIPAPIUrlForIP(); // Generate URI for current IP
        if (CommonClass.getConfig().usePlayerIP() && IPAPIURI != null)
        {
            // Make API request
            HttpRequest requestIPApi = HttpRequest.newBuilder(IPAPIURI).GET().build();

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

                    Constants.LOG.info("Got Lat: {} Lon: {} from IP: {} Area: {}", lat_ip, lon_ip, Constants.USER_IP, area);
                }
            });
        }
    }
}
