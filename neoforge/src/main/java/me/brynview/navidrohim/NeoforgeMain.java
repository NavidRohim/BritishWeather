package me.brynview.navidrohim;

import me.brynview.navidrohim.common.DefaultConfig;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import me.brynview.navidrohim.common.config.NeoforgeConfigSerializableValues;
import me.brynview.navidrohim.common.config.NeoforgeConfig;
import me.brynview.navidrohim.platform.Services;
import me.brynview.navidrohim.server.config.CommonConfig;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class NeoforgeMain
{
    public NeoforgeMain()
    {
        CommonConfig config = initConfig();
        BritishWeather.init(config, (server) ->
        {
            WeatherUpdatePacket packet = new WeatherUpdatePacket(BritishWeather.getWeatherManager().getState().getWeatherCondition());
            server.getPlayerList().broadcastAll(packet);
        });
    }

    private CommonConfig initConfig()
    {
        if (Services.PLATFORM.isModLoaded(Constants.YACL_MOD_ID))
        {
            NeoforgeConfigSerializableValues.HANDLER.load();
            return new NeoforgeConfig();
        } else {
            return new DefaultConfig();
        }
    }
}