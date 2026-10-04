package me.brynview.navidrohim.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.PersistentEntriesManager;
import me.brynview.navidrohim.client.screen.PinCreationScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ClientCommon
{
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "keybinds"));
    public static final KeyMapping PROVIDER = new KeyMapping(
            "br.keybind.showProviders", // The translation key for the key mapping.
            InputConstants.KEY_LALT, // The keycode of the key.
            CATEGORY // The category of the mapping.
    );

    public static final KeyMapping DEBUG_ON_PRESS = new KeyMapping(
            "br.keybind.debug",
            InputConstants.KEY_COMMA,
            CATEGORY
    );

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

        if (DEBUG_ON_PRESS.isDown())
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
