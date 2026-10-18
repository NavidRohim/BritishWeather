package me.brynview.navidrohim.client.hud.compass.entry.iapi.entry;

import net.minecraft.world.phys.Vec3;

public interface EntryRenderable
{
    String getMarker();

    int getMarkerHalfWidth();

    int getHighlightColor();

    int getColor();

    boolean isPersistent();

    Vec3 getPosition();

    boolean shouldShowDistance();

    DefaultEntry.RenderState getRenderState();
}
