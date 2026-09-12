package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonModConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;

public class FabricCommonModConfig implements CommonModConfig
{

    @Override
    public Float getLongitude()
    {
        return FabricNativeModConfig.lon;
    }

    @Override
    public Float getLatitude()
    {
        return FabricNativeModConfig.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return FabricNativeModConfig.fetchWeatherStatusIntervalInSeconds * 20; // Convert to ticks
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return WeatherLocationSources.getSource(FabricNativeModConfig.locationOptions.getKey());
    }

    @Override
    public String getPostcode()
    {
        return FabricNativeModConfig.postcode;
    }
}
