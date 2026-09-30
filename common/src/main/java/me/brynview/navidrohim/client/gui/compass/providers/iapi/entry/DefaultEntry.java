package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DefaultEntry implements CompassEntry
{
    private final Vec3 position;
    private final String level;

    private final Minecraft mc;

    private String marker;
    private int markerWidthHalf;

    public DefaultEntry(Vec3 position, String level, String marker)
    {
        this.mc = Minecraft.getInstance();

        this.level = level;
        this.position = position;
        this.marker = marker;


        this.markerWidthHalf = mc.font.width(marker) / 2;
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public String getLevel()
    {
        return level;
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

    public static DefaultEntry of(Vec3 position, String level, String marker)
    {
        return new DefaultEntry(position, level, marker);
    }

    public boolean isPersistent()
    {
        return false;
    }

    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
    }

    public final String getId()
    {
        if (!(this instanceof Singleton))
        {
            return "%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.position.x, (int) this.position.z, level);
        }
        return this.getClass().getSimpleName();
    }
}
