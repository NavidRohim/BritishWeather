package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import me.brynview.navidrohim.client.gui.compass.HudCompass;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface CompassProvider
{
    Collection<DefaultCompassProviderEntry> getEntries();

    default void startTick(LocalPlayer player) {}
    default void stopTick(LocalPlayer player) {}
    default void tick(@NotNull LocalPlayer player) {}

    default boolean shouldRender()
    {
        return true;
    }

    default boolean hasExpired() { return false; }

    default boolean shouldShowDistance()
    {
        return true;
    }

    default int getColor()
    {
        return HudCompass.OBJECTIVE_MARKER_COLOUR;
    }
}
