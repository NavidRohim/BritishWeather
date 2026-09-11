package me.brynview.navidrohim.server.weather.sources;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.weather.ServerWeatherManager;
import me.brynview.navidrohim.server.weather.WeatherCache;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;

public interface WeatherLocationSource
{
    record Location(float lat, float lon, String name) {}

    void getLocation(TriConsumer<Location, WeatherLocationSource, String> callable);

    default String getKey()
    {
        return "";
    }

    @Nullable
    default WeatherCache getCache()
    {
        return null;
    }

    default void setCachedWeatherState(String cacheKey, ServerWeatherManager.WeatherState weatherState)
    {
        if (this.shouldHaveCache() && (this.getCache().isNotCached(cacheKey)))
        {
            getCache().setCachedWeatherState(cacheKey, weatherState);
        }
    }

    default void setCache(WeatherCache weatherCache)
    {

    }

    default boolean shouldHaveCache()
    {
        return getCache() != null;
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
