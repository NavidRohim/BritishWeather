package me.brynview.navidrohim.client.keybinds;

import me.brynview.navidrohim.client.ClientCommon;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class ModKeybinds
{
    public static void init()
    {
        KeyMappingHelper.registerKeyMapping(ClientCommon.PROVIDER);
    }
}
