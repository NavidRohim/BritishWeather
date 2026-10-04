package me.brynview.navidrohim.util;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

public class GeneralUtils
{
    public static String getDimensionFromPlayer(@NotNull LocalPlayer player)
    {
        return player.level().dimension().identifier().toString();
    }
}
