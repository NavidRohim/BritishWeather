package me.brynview.navidrohim.client.hud.compass.entry.iapi.entry;

import net.minecraft.world.phys.Vec3;

public interface EntryRenderable
{
    String getMarker();

    int getMarkerHalfWidth();

    int getColour();

    boolean isPersistent();

    Vec3 getPosition();
}
