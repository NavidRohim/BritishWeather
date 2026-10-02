package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/*
* DefaultEntry should be extended but is not abstract as it does have functionality by itself.
* An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
*/
public class DefaultEntry implements CompassEntry
{
    private final Vec3 position; // Position of the entry in-game.
    private final String level; // What dimension the entry was made in.
    private final String entryId; // Unique entry ID. Not used for anything at the moment.

    private final Minecraft mc;

    private String marker; // What text will be rendered on the compass.
    private int markerWidthHalf; // The markers width halved.

    public DefaultEntry(Vec3 position, String level, String marker)
    {
        this.mc = Minecraft.getInstance();
        this.level = level;
        this.position = position;

        if (!(this instanceof Singleton))
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = "%s.%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.position.x, (int) this.position.y, (int) this.position.z, level);
        } else {
            // If singleton, it is just the class name so it can be overridden.
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
        this.markerWidthHalf = mc.font.width(marker) / 2; // Calculate the markers width here, so it does not have to be calculated every render pass.
    }

    public int getMarkerWidthHalf()
    {
        return markerWidthHalf;
    }

    /*
    If the entry is persistent through game sessions.
    If true, the entry will be stored in an NBT file in the root .minecraft directory, and the filename will be the world folder name.
     */
    public boolean isPersistent()
    {
        return false;
    }

    public final String getId()
    {
        return this.entryId;
    }

    /*
    This method exists so other custom data can be persistent when saved to NBT file.
    Anything can be written here.
     */
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
    }

    public static DefaultEntry of(Vec3 position, String level, String marker)
    {
        return new DefaultEntry(position, level, marker);
    }
}
