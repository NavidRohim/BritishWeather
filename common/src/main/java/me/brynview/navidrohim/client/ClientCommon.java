package me.brynview.navidrohim.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.HudCompass;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
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

    private static ClientWeatherManager WEATHER_MANAGER;

    public static void init()
    {
        WEATHER_MANAGER = new ClientWeatherManager();
    }

    public static ClientWeatherManager getWeatherManager()
    {
        return WEATHER_MANAGER;
    }

    public static void tickClient(@NotNull LocalPlayer player)
    {
        HudCompass.tick(player);
    }
}
