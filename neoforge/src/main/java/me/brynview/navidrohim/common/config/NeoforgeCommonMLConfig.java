package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.client.config.NeoforgeNativeModConfig;
import me.brynview.navidrohim.server.config.CommonMLConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;

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
        return WeatherLocationSources.getSource(NeoforgeCommonSideConfig.locationOptions);
    }

    @Override
    public String getPostcode()
    {
        return NeoforgeCommonSideConfig.postcode;
    }

    @Override
    public boolean shouldShowLocationToClients()
    {
        return NeoforgeCommonSideConfig.shouldShowLocationToClients;
    }
}
