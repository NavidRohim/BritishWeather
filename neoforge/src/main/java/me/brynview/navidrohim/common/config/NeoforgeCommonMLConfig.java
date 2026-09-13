package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonMLConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;

public class NeoforgeCommonMLConfig implements CommonMLConfig
{
    @Override
    public Float getLongitude()
    {
        return NeoforgeCommonSideConfig.lon;
    }

    @Override
    public Float getLatitude()
    {
        return NeoforgeCommonSideConfig.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return NeoforgeCommonSideConfig.weatherStatusRefreshIntervalTicks;
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return NeoforgeCommonSideConfig.locationOptionsSrc;
    }

    @Override
    public String getPostcode()
    {
        return NeoforgeCommonSideConfig.postcode;
    }
}
