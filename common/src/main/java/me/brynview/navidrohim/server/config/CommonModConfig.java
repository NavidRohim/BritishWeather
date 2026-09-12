package me.brynview.navidrohim.server.config;

import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;

public interface CommonModConfig
{
    Float getLongitude();

    Float getLatitude();

    Integer getWeatherFetchIntervalInTicks();

    WeatherLocationSource getLocationSource();

    String getPostcode();
}
