package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonMLConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;

public class FabricCommonMLConfig implements CommonMLConfig
{

    @Override
    public Float getLongitude()
    {
        return FabricCommonSideConfig.lon;
    }

    @Override
    public Float getLatitude()
    {
        return FabricCommonSideConfig.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return FabricCommonSideConfig.weatherStatusRefreshIntervalTicks + 1; // Convert to ticks
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return WeatherLocationSources.getSource(FabricCommonSideConfig.locationOptions);
    }

    @Override
    public String getPostcode()
    {
        return FabricCommonSideConfig.postcode;
    }

    @Override
    public boolean shouldShowLocationToClients()
    {
        return FabricCommonSideConfig.shouldShowLocationToClients;
    }
}
