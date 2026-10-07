package me.brynview.navidrohim.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ClientKeybinds
{
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "keybinds"));

    public static final KeyMapping ZOOM_MODIFIER = new KeyMapping(
            "br.keybind.zoom_modifier",
            InputConstants.KEY_LSHIFT,
            CATEGORY
    );

    public static final KeyMapping DEBUG_ON_PRESS = new KeyMapping(
            "br.keybind.debug",
            InputConstants.KEY_COMMA,
            CATEGORY
    );

    public static final KeyMapping ENTRY_HUD_KEY = new KeyMapping(
            "br.keybind.showPins", // The translation key for the key mapping.
            InputConstants.KEY_LALT, // The keycode of the key.
            CATEGORY // The category of the mapping.
    );

    public static final KeyMapping DELETION = new KeyMapping(
            "br.keybind.delete_pin",
            InputConstants.KEY_BACKSPACE,
            CATEGORY
    );

    public static final KeyMapping CREATE = new KeyMapping(
            "br.keybind.create_pin",
            InputConstants.KEY_N,
            CATEGORY
    );

    public static final KeyMapping LEFT_SNAP = new KeyMapping(
            "br.keybind.snap_left",
            InputConstants.KEYCODE_RIGHT,
            CATEGORY
    );

    public static final KeyMapping RIGHT_SNAP = new KeyMapping(
            "br.keybind.snap_right",
            InputConstants.KEYCODE_RIGHT,
            CATEGORY
    );
}
