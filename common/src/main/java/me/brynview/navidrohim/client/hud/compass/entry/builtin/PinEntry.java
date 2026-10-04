package me.brynview.navidrohim.client.hud.compass.entry.builtin;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/*
Normal pin to indicate a POI or any other place of significance.
 */
public class PinEntry extends DefaultEntry
{
    private boolean isPersistent;

    public PinEntry(Vec3 position, String level, String pinIcon, boolean isPersistent, int colour)
    {
        // "P" as the marker is a placeholder.
        super(position, level, pinIcon);
        this.isPersistent = isPersistent;

    }

    /*
    Constructor for NBT deserialisation
     */
    public PinEntry(String s, Vec3 vec3, String level, FriendlyByteBuf extraData)
    {
        super(vec3, level, s);
        this.isPersistent = extraData.readBoolean();
    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeBoolean(isPersistent);
    }

    @Override
    public boolean isPersistent()
    {
        return isPersistent;
    }
}
