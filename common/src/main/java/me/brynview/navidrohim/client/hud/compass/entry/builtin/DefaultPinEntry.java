package me.brynview.navidrohim.client.hud.compass.entry.builtin;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.Pin;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/*
Normal pin to indicate a POI or any other place of significance.
 */
public class DefaultPinEntry extends DefaultEntry implements Pin
{
    private boolean isPersistent;
    private boolean shouldShowDistance;
    private boolean shouldDisappearWhenNotInView;

    public DefaultPinEntry(Vec3 position, String level, String pinIcon, boolean isPersistent, int colour, boolean showDistance, boolean shouldDisappearWhenNotInView)
    {
        // "P" as the marker is a placeholder.
        super(position, level, pinIcon, colour);
        this.isPersistent = isPersistent;
        this.shouldShowDistance = showDistance;
        this.shouldDisappearWhenNotInView = shouldDisappearWhenNotInView;
    }

    /*
    Constructor for NBT deserialisation
     */
    public DefaultPinEntry(String s, int colour, Vec3 vec3, String level, FriendlyByteBuf extraData)
    {
        super(vec3, level, s, colour);
        this.isPersistent = extraData.readBoolean();
        this.shouldShowDistance = extraData.readBoolean();
        this.shouldDisappearWhenNotInView = extraData.readBoolean();
    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeBoolean(isPersistent);
        friendlyByteBuf.writeBoolean(shouldShowDistance);
        friendlyByteBuf.writeBoolean(shouldDisappearWhenNotInView);
    }

    @Override
    public boolean isPersistent()
    {
        return isPersistent;
    }

    @Override
    public boolean shouldDisappearWhenNotInView()
    {
        return shouldDisappearWhenNotInView;
    }

    @Override
    public boolean shouldShowDistance()
    {
        return shouldShowDistance;
    }
}
