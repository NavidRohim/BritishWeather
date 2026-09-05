package me.brynview.navidrohim.client;

import me.brynview.navidrohim.common.WeatherCondition;

public class ClientWeatherManager
{
    private WeatherCondition WEATHER_CONDITION = WeatherCondition.CLOUDY;

    public void setWeather(WeatherCondition weather)
    {
        WEATHER_CONDITION = weather;
    }

    public WeatherCondition getWeather()
    {
        return WEATHER_CONDITION;
    }
}
