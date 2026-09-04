package me.brynview.navidrohim.client;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.server.WeatherManager;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientCommon
{

    public static WeatherManager.WeatherState SERVER_WEATHER_STATE = CommonClass.DEBUG_MASTER_WEATHER_STATE;

    public static void clientLevelTick(ClientLevel level)
    {
        WeatherHail.tickWeatherHail(level);
    }
}
