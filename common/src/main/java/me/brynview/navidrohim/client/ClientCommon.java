package me.brynview.navidrohim.client;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

public class ClientCommon
{
    private static String currentServerName = "";
    private static ClientWeatherManager WEATHER_MANAGER;
    public static Compass compass;

    public static final SoundEvent SCROLL = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "scroll"));

    public static void init()
    {
        WEATHER_MANAGER = new ClientWeatherManager();
        compass = Compass.init();


    }

    public static ClientWeatherManager getWeatherManager()
    {
        return WEATHER_MANAGER;
    }

    public static void tickClient(@NotNull LocalPlayer player)
    {
        Compass.tick(player);
    }

    /*
    Call then when the client level changes (transferring dimension, loading into a world or server)
     */
    public static void joinServerEvent()
    {
        ClientCommon.WEATHER_MANAGER.reset();
        EntryManager.ProviderRegistry.clear();
        EntryManager.load();
    }

    public static void leaveServerEvent()
    {
        EntryManager.save();
        EntryManager.ProviderRegistry.clear();
    }

}
