package me.brynview.navidrohim.client.hud.compass.entry.builtin;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.Pin;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.TimeMethod;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultTimedEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/*
Same as PinEntry but timed.
 */
public class TimedPinEntry extends DefaultTimedEntry implements Pin
{
    private boolean isPersistent;
    private boolean shouldShowDistance;
    private boolean shouldDisappearWhenNotInView;

    public TimedPinEntry(Vec3 position, String level, String marker, TimeMethod timeMethod, boolean isPersistent, int colour, boolean shouldShowDistance, boolean shouldDisappearWhenNotInView)
    {
        super(position, level, marker, timeMethod, colour);
        this.isPersistent = isPersistent;
        this.shouldShowDistance = shouldShowDistance;
        this.shouldDisappearWhenNotInView = shouldDisappearWhenNotInView;
    }

    public TimedPinEntry(String s, int colour, Vec3 vec3, String level, FriendlyByteBuf friendlyByteBuf)
    {
        String savedTimeMethod = friendlyByteBuf.readUtf();
        TimeMethod.TimeMethods tm = TimeMethod.TimeMethods.valueOf(savedTimeMethod);
        TimeMethod remainingTimeMethod = tm.getFromBuffer(friendlyByteBuf);

        this.isPersistent = friendlyByteBuf.readBoolean();
        this.shouldShowDistance = friendlyByteBuf.readBoolean();

        super(vec3, level, s, remainingTimeMethod, colour);

    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        super.serialise(friendlyByteBuf);
        friendlyByteBuf.writeBoolean(isPersistent);
        friendlyByteBuf.writeBoolean(shouldShowDistance);
    }

    @Override
    public boolean isPersistent()
    {
        return isPersistent;
    }

    @Override
    public boolean shouldShowDistance()
    {
        return shouldShowDistance;
    }

    @Override
    public boolean shouldDisappearWhenNotInView()
    {
        return shouldDisappearWhenNotInView;
    }
}
