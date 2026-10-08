package me.brynview.navidrohim.client.hud.compass.entry.iapi.entry;

import net.minecraft.world.phys.Vec3;

public interface EntryRenderable
{
    String getMarker();

    int getMarkerHalfWidth();

    void setFocused(boolean focused);

    boolean isFocused();

    int getColour();

    int getHighlightColour();

    boolean isPersistent();

    Vec3 getPosition();
}
