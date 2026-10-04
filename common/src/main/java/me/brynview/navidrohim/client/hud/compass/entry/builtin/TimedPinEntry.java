package me.brynview.navidrohim.client.hud.compass.entry.builtin;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.TimeMethod;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultTimedEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/*
Same as PinEntry but timed.
 */
public class TimedPinEntry extends DefaultTimedEntry
{
    private boolean isPersistent;

    public TimedPinEntry(Vec3 position, String level, String marker, TimeMethod timeMethod, boolean isPersistent, int colour)
    {
        super(position, level, marker, timeMethod, colour);
        this.isPersistent = isPersistent;
    }

    public TimedPinEntry(String s, int colour, Vec3 vec3, String level, FriendlyByteBuf friendlyByteBuf)
    {
        String savedTimeMethod = friendlyByteBuf.readUtf();
        TimeMethod.TimeMethods tm = TimeMethod.TimeMethods.valueOf(savedTimeMethod);
        TimeMethod remainingTimeMethod = tm.getFromBuffer(friendlyByteBuf);

        this.isPersistent = friendlyByteBuf.readBoolean();

        super(vec3, level, s, remainingTimeMethod, colour);

    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        super.serialise(friendlyByteBuf);
        friendlyByteBuf.writeBoolean(isPersistent);
    }

    @Override
    public boolean isPersistent()
    {
        return isPersistent;
    }
}
