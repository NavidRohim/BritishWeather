package me.brynview.navidrohim.platform.services;

public interface CommonModConfig
{
    Float getLongitude();

    Float getLatitude();

    Integer getWeatherFetchIntervalInTicks();

    Boolean usePlayerIP();
}
