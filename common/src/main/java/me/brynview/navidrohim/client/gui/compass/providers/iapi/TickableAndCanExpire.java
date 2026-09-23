package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

public interface TickableAndCanExpire
{
    default void tick(@NotNull LocalPlayer player) {}
    default void displayTick(@NotNull LocalPlayer player) {}
    default void startTick(@NotNull LocalPlayer player) {}
    default void endTick(@NotNull LocalPlayer player) {}

    default boolean hasExpired()
    {
        return false;
    }

}
