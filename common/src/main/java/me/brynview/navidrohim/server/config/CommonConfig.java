package me.brynview.navidrohim.server.config;

import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;

/*
Any new mod loaders should implement this
 */
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

    boolean shouldRenderHeading();

    boolean shouldShowEntitiesOnCompass();

    boolean shouldShowBuiltinObjectives();

    int getCompassSize();

    int getCompassY();
}
