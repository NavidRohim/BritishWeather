package me.brynview.navidrohim.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeybinds
{
    public static void init()
    {
        KeyMappingHelper.registerKeyMapping(ClientCommon.PROVIDER);
    }
}
