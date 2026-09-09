package me.brynview.navidrohim.server.weather.sources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.weather.WeatherCache;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static me.brynview.navidrohim.server.weather.ServerWeatherManager.HTTP_CLIENT;

public interface WeatherLocationSource
{
    record Location(float lat, float lon, String name) {}

    void getLocation(TriConsumer<Location, WeatherLocationSource, String> callable);

    @Nullable
    default WeatherCache getCache()
    {
        return null;
    }

    @Nullable
    default WeatherCache.TypeAndCodec getTypeAndCodec()
    {
        return null;
    }

    default void setCache(WeatherCache cache)
    {

    }

    default boolean hasCache()
    {
        return false;
    }

    final class ManualLocationSource implements WeatherLocationSource
    {
        @Override
        public void getLocation(TriConsumer<Location, WeatherLocationSource, String> callable)
        {
            Constants.LOG.info("Getting location information via Lat/Long manual");
            callable.accept(new Location(BritishWeather.getConfig().getLatitude(), BritishWeather.getConfig().getLongitude(), Constants.DEFAULT_LOCATION_NAME), this, "");
        }
    }

}
