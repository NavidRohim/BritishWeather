package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class DefaultCompassProviderEntry
{
    private final Vec3 position;
    private final String marker;
    private final int markerWidthHalf;

    public DefaultCompassProviderEntry(Vec3 position, String marker)
    {
        Minecraft mc = Minecraft.getInstance();

        this.position = position;
        this.marker = marker;
        this.markerWidthHalf = mc.font.width(marker) / 2;
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public String getMarker()
    {
        return marker;
    }

    public int getMarkerWidthHalf()
    {
        return markerWidthHalf;
    }

    public static DefaultCompassProviderEntry of(Vec3 position, String marker)
    {
        return new DefaultCompassProviderEntry(position, marker);
    }
}
