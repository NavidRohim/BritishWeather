package me.brynview.navidrohim.platform.services;

import me.brynview.navidrohim.server.locationsource.LocationSource;

public interface CommonModConfig
{
    Float getLongitude();

    Float getLatitude();

    Integer getWeatherFetchIntervalInTicks();

    Boolean usePlayerIP();

    LocationSource getLocationSource();
}
