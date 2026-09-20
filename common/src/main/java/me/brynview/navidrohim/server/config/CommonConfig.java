package me.brynview.navidrohim.server.config;

import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;

public interface CommonConfig
{
    Float getLongitude();

    Float getLatitude();

    Integer getWeatherFetchIntervalInTicks();

    WeatherLocationSource getLocationSource();

    String getPostcode();

    boolean shouldShowLocationToClients();

    boolean hailShouldDealDamage();

    boolean debug();

    boolean shouldRenderCompass();

    int getCompassSize();

    int getCompassY();
}
