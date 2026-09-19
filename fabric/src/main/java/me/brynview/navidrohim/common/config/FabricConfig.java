package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;

public class FabricConfig implements CommonConfig
{

    @Override
    public Float getLongitude()
    {
        return FabricConfigSerializer.lon;
    }

    @Override
    public Float getLatitude()
    {
        return FabricConfigSerializer.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return FabricConfigSerializer.weatherStatusRefreshIntervalTicks + 1; // Convert to ticks
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return WeatherLocationSources.getSource(FabricConfigSerializer.locationOptions);
    }

    @Override
    public String getPostcode()
    {
        return FabricConfigSerializer.postcode;
    }

    @Override
    public boolean shouldShowLocationToClients()
    {
        return FabricConfigSerializer.shouldShowLocationToClients;
    }

    @Override
    public boolean hailShouldDealDamage()
    {
        return FabricConfigSerializer.hailShouldDealDamage;
    }

    @Override
    public boolean debug()
    {
        return FabricConfigSerializer.debug;
    }

    @Override
    public boolean shouldRenderCompass()
    {
        return FabricConfigSerializer.shouldRenderCompass;
    }

    @Override
    public int getCompassSize()
    {
        return FabricConfigSerializer.compassSize;
    }
}
