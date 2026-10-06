package me.brynview.navidrohim.client;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.PersistentEntriesManager;
import me.brynview.navidrohim.client.screen.PinCreationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

public class ClientCommon
{
    private static ClientWeatherManager WEATHER_MANAGER;
    public static Compass compass;

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

        if (ClientKeybinds.ENTRY_HUD_KEY.isDown() && ClientKeybinds.CREATE.isDown())
        {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreenAndShow(new PinCreationScreen(mc, player));
        }
    }

    /*
    Call then when the client level changes (transferring dimension, loading into a world or server)
     */
    public static void changeLevel()
    {
        PersistentEntriesManager.ProviderRegistry.clear();
        PersistentEntriesManager.load();
    }
}
