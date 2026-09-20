package me.brynview.navidrohim.common;

import io.netty.util.Constant;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.config.CommonConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;

public class DefaultConfig implements CommonConfig
{

    public DefaultConfig()
    {
        Constants.LOG.info("Using default config as YACL is not installed.");
    }

    @Override
    public Float getLongitude()
    {
        return Constants.DefaultConfigValues.longitude;
    }

    @Override
    public Float getLatitude()
    {
        return Constants.DefaultConfigValues.latitude;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return Constants.DefaultConfigValues.weatherRefreshInterval;
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        return Constants.DefaultConfigValues.defaultLocationMethod;
    }

    @Override
    public String getPostcode()
    {
        return Constants.DefaultConfigValues.postcode;
    }

    @Override
    public boolean shouldShowLocationToClients()
    {
        return Constants.DefaultConfigValues.shouldShowLocationToClients;
    }

    @Override
    public boolean hailShouldDealDamage()
    {
        return Constants.DefaultConfigValues.hailShouldDealDamage;
    }

    @Override
    public boolean debug()
    {
        return Constants.DefaultConfigValues.debug;
    }

    @Override
    public boolean shouldRenderCompass()
    {
        return Constants.DefaultConfigValues.shouldRenderCompass;
    }

    @Override
    public int getCompassSize()
    {
        return Constants.DefaultConfigValues.compassSize;
    }

    @Override
    public int getCompassY()
    {
        return Constants.DefaultConfigValues.compassY;
    }
}
