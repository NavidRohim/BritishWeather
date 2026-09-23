package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class DefaultEntry implements CompassEntry
{
    private final Vec3 position;
    private final Minecraft mc;

    private String marker;
    private int markerWidthHalf;

    public DefaultEntry(Vec3 position, String marker)
    {
        this.mc = Minecraft.getInstance();

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

    public void setMarker(String marker)
    {
        this.marker = marker;
        this.markerWidthHalf = mc.font.width(marker) / 2;
    }

    public int getMarkerWidthHalf()
    {
        return markerWidthHalf;
    }

    public static DefaultEntry of(Vec3 position, String marker)
    {
        return new DefaultEntry(position, marker);
    }
}
