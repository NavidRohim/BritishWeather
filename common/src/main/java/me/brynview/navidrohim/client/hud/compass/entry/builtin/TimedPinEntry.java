package me.brynview.navidrohim.client.hud.compass.entry.builtin;

import me.brynview.navidrohim.client.hud.compass.entry.builtin.time.GameTime;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultTimedEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;

/*
Same as PinEntry but timed.
 */
public class TimedPinEntry extends DefaultTimedEntry
{
    private boolean isPersistent;

    public TimedPinEntry(Vec3 position, String level, String marker, TimeUnit unit, int duration, boolean isPersistent)
    {
        super(position, level, marker, new GameTime(unit, duration));
        this.isPersistent = isPersistent;
    }

    public TimedPinEntry(String s, Vec3 vec3, String level, FriendlyByteBuf friendlyByteBuf)
    {
        GameTime gameTime = GameTime.decode(friendlyByteBuf);
        this.isPersistent = friendlyByteBuf.readBoolean();

        super(vec3, level, s, gameTime);

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
