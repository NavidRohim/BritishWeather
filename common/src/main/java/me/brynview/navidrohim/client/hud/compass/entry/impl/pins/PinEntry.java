package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import me.brynview.navidrohim.client.hud.compass.entry.entry.DefaultEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class PinEntry extends DefaultEntry
{
    protected boolean isPersistent;
    protected boolean shouldShowDistance;
    protected boolean shouldDisappearWhenNotInView;

    public PinEntry(Vec3 position, String level, String marker, int colour, boolean isPersistent, boolean shouldShowDistance, boolean shouldDisappearWhenNotInView)
    {
        super(position, level, marker, colour);
        this.isPersistent = isPersistent;
        this.shouldShowDistance = shouldShowDistance;
        this.shouldDisappearWhenNotInView = shouldDisappearWhenNotInView;
    }

    public PinEntry(String marker, int colour, Vec3 pos, String level, FriendlyByteBuf friendlyByteBuf)
    {
        super(pos, level, marker, colour);

        this.isPersistent = friendlyByteBuf.readBoolean();
        this.shouldShowDistance = friendlyByteBuf.readBoolean();
        this.shouldDisappearWhenNotInView = friendlyByteBuf.readBoolean();
    }
    @Override
    public boolean isPersistent()
    {
        return isPersistent;
    }

    public void setPersistent(boolean persistent)
    {
        isPersistent = persistent;
    }

    @Override
    public boolean shouldShowDistance()
    {
        return shouldShowDistance;
    }

    public void setShouldShowDistance(boolean shouldShowDistance)
    {
        this.shouldShowDistance = shouldShowDistance;
    }

    @Override
    public boolean shouldDisappearWhenNotInView()
    {
        return shouldDisappearWhenNotInView;
    }

    public void setShouldDisappearWhenNotInView(boolean shouldDisappearWhenNotInView)
    {
        this.shouldDisappearWhenNotInView = shouldDisappearWhenNotInView;
    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeBoolean(isPersistent);
        friendlyByteBuf.writeBoolean(shouldShowDistance);
        friendlyByteBuf.writeBoolean(shouldDisappearWhenNotInView);
    }
}
