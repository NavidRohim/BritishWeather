package me.brynview.navidrohim.server.weather.sources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.weather.WeatherCache;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static me.brynview.navidrohim.server.weather.ServerWeatherManager.HTTP_CLIENT;

public final class IPLocationSource implements WeatherLocationSource
{
    private static final String key = "ip";
    private static WeatherCache CACHE = null;

    @Override
    public @Nullable WeatherCache getCache()
    {
        return CACHE;
    }

    @Override
    public void setCache(WeatherCache cache)
    {
        CACHE = cache;
    }

    @Override
    public boolean shouldHaveCache()
    {
        return true;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public void getLocation(TriConsumer<Location, WeatherLocationSource, String> callable)
    {
        if (getCache().isNotCached(Constants.USER_IP))
        {
            HttpRequest requestIPApi = HttpRequest.newBuilder(Constants.IP_API_ENDPOINT).GET().build();

            // Send request asynchronously. Check status code is valid and API request is successful. View docs here
            // https://ip-api.com/docs/api:json
            HTTP_CLIENT.sendAsync(requestIPApi, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> {

                int statusCode = stringHttpResponse.statusCode();
                JsonObject returnedObj = JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject();
                // TODO error handling
                if (statusCode == 200 && returnedObj.get("status").getAsString().equals("success"))
                {
                    float lat_ip = returnedObj.get("lat").getAsFloat();
                    float lon_ip = returnedObj.get("lon").getAsFloat();
                    String area = returnedObj.get("city").getAsString() + returnedObj.get("district").getAsString(); // "district" may be irrelevant in the UK?
                    Location location = new Location(lat_ip, lon_ip, area);

                    Constants.LOG.info("Getting location information via IP + " + location);
                    callable.accept(location, this, Constants.USER_IP);
                }
            });
        } else
        {
            if (!getCache().getCachedWeatherState().isEmpty())
            {
                Constants.LOG.info("Getting location information via IP cache.");
                callable.accept(getCache().getCachedWeatherState().getLocation(), this, Constants.USER_IP);
            } else if (getCache().getCachedWeatherState() != null)
            {
                Constants.LOG.warn("Using cached weather state when getState() is not present! This is very bad!");
                callable.accept(BritishWeather.getCache().getCachedWeatherState().getLocation(), this, Constants.USER_IP);
            } else
            {
                throw new RuntimeException("No valid weather state found for IP!");
            }

        }
    }
}
