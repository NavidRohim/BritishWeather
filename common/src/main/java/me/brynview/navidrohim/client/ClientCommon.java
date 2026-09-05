package me.brynview.navidrohim.client;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.server.ServerWeatherManager;
import net.minecraft.client.multiplayer.ClientLevel;

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
