package me.brynview.navidrohim.platform.services;

import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;

public interface CommonModConfig
{
    Float getLongitude();

    Float getLatitude();

    Integer getWeatherFetchIntervalInTicks();

    WeatherLocationSource getLocationSource();

    String getPostcode();
}
