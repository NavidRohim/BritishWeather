package me.brynview.navidrohim.client;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.common.WeatherCondition;

/*
This seems a bit unnecessary
 */
public class ClientWeatherManager
{
    private WeatherCondition WEATHER_CONDITION = WeatherCondition.CLOUDY;

    public void setWeather(WeatherCondition weather)
    {
        WEATHER_CONDITION = weather;
        Constants.debug("Setting weather to {} on client", weather);
    }

    public WeatherCondition getWeather()
    {
        return WEATHER_CONDITION;
    }

    public void reset()
    {
        setWeather(WeatherCondition.DEFAULT);
        Constants.debug("Resetting client weather manager");
    }
}
