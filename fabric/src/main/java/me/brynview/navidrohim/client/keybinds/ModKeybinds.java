package me.brynview.navidrohim.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeybinds
{

    public static KeyMapping showProvidersKey;

    public static void init()
    {
        KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "keybinds"));
        ModKeybinds.showProvidersKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "br.keybind.showProviders", // The translation key for the key mapping.
                        InputConstants.KEY_LALT, // The keycode of the key.
                        CATEGORY // The category of the mapping.
                ));

    }
}
