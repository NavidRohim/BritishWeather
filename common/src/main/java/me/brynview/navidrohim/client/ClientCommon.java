package me.brynview.navidrohim.client;

public class ClientCommon
{
    private static ClientWeatherManager WEATHER_MANAGER;

    public static void init()
    {
        WEATHER_MANAGER = new ClientWeatherManager();
    }

    public static ClientWeatherManager getWeatherManager()
    {
        return WEATHER_MANAGER;
    }
}
