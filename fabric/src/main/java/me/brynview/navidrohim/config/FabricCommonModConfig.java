package me.brynview.navidrohim.config;

import me.brynview.navidrohim.platform.services.CommonModConfig;

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
    public Boolean usePlayerIP()
    {
        return FabricNativeModConfig.usePlayerIP;
    }
}
