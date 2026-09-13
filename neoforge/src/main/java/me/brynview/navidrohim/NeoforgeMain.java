package me.brynview.navidrohim;

import me.brynview.navidrohim.common.WeatherUpdatePacket;
import me.brynview.navidrohim.common.config.NeoforgeCommonSideConfig;
import me.brynview.navidrohim.common.config.NeoforgeCommonMLConfig;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class NeoforgeMain
{
    public NeoforgeMain()
    {
        NeoforgeCommonSideConfig.HANDLER.load();
        BritishWeather.init(new NeoforgeCommonMLConfig(), (server) ->
        {
            WeatherUpdatePacket packet = new WeatherUpdatePacket(BritishWeather.getWeatherManager().getState().getWeatherCondition());
            server.getPlayerList().broadcastAll(packet);
        });
    }
}