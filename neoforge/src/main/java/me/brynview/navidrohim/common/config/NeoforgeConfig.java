package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;

public class NeoforgeConfig implements CommonConfig
{
    @Override
    public Float getLongitude()
    {
        return NeoforgeConfigSerializableValues.lon;
    }

    @Override
    public Float getLatitude()
    {
        return NeoforgeConfigSerializableValues.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return NeoforgeConfigSerializableValues.weatherStatusRefreshIntervalTicks;
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return WeatherLocationSources.getSource(NeoforgeConfigSerializableValues.locationOptions);
    }

    @Override
    public String getPostcode()
    {
        return NeoforgeConfigSerializableValues.postcode;
    }

    @Override
    public boolean shouldShowLocationToClients()
    {
        return NeoforgeConfigSerializableValues.shouldShowLocationToClients;
    }

    @Override
    public boolean hailShouldDealDamage()
    {
        return NeoforgeConfigSerializableValues.hailShouldDealDamage;
    }

    @Override
    public boolean debug()
    {
        return NeoforgeConfigSerializableValues.debug;
    }

    @Override
    public boolean shouldRenderCompass()
    {
        return NeoforgeConfigSerializableValues.shouldRenderCompass;
    }

    @Override
    public boolean shouldRenderHeading()
    {
        return NeoforgeConfigSerializableValues.shouldRenderHeading;
    }

    @Override
    public int getCompassSize()
    {
        return NeoforgeConfigSerializableValues.compassSize;
    }

    @Override
    public int getCompassY()
    {
        return NeoforgeConfigSerializableValues.compassY;
    }
}
