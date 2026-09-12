package me.brynview.navidrohim.server.config;

import me.brynview.navidrohim.platform.services.CommonModConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;

public class NeoforgeCommonModConfig implements CommonModConfig
{
    @Override
    public Float getLongitude()
    {
        return NeoforgeNativeModConfig.lon;
    }

    @Override
    public Float getLatitude()
    {
        return NeoforgeNativeModConfig.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return NeoforgeNativeModConfig.fetchWeatherStatusIntervalInSeconds;
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return WeatherLocationSources.getSource(NeoforgeNativeModConfig.locationOptions.getKey());
    }

    @Override
    public String getPostcode()
    {
        return NeoforgeNativeModConfig.postcode;
    }
}
