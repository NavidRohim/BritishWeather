package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DefaultEntry implements CompassEntry
{
    private final Vec3 position;
    private final String level;
    private final String entryId;

    private final Minecraft mc;

    private String marker;
    private int markerWidthHalf;

    public DefaultEntry(Vec3 position, String level, String marker)
    {
        this.mc = Minecraft.getInstance();
        this.level = level;
        this.position = position;

        if (!(this instanceof Singleton))
        {
            this.entryId = "%s.%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.position.x, (int) this.position.y, (int) this.position.z, level);
        } else {
            this.entryId = this.getClass().getSimpleName();
        }

        this.setMarker(marker);
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

    public boolean isPersistent()
    {
        return false;
    }

    public final String getId()
    {
        return this.entryId;
    }

    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
    }

    public static DefaultEntry of(Vec3 position, String level, String marker)
    {
        return new DefaultEntry(position, level, marker);
    }
}
