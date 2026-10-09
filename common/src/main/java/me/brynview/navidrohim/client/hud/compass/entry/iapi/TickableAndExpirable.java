package me.brynview.navidrohim.client.hud.compass.entry.iapi;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

/*
Interface that makes entries tickable, and can expire.
"Expire" meaning, when an entry is expired it will be removed from the compass.

Ticks for compass entries can be used to calculate states. Like making the entry flash, change colour, etc..
Possibilities are endless.
 */
public interface TickableAndExpirable
{
    default void tick(@NotNull LocalPlayer player) {}
    default void startTick(@NotNull LocalPlayer player) {}
    default void endTick(@NotNull LocalPlayer player) {}

    default boolean hasExpired()
    {
        return false;
    }

}