package me.brynview.navidrohim.client.gui.compass.providers.builtin.time;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.TimeMethod;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.TimeUnit;

public class RealTime implements TimeMethod
{
    private long endTime;

    public RealTime(TimeUnit unit, int duration)
    {
        this.endTime = System.nanoTime() + unit.toNanos(duration);
    }

    public RealTime(long remaining)
    {
        this.endTime = remaining;
    }

    @Override
    public boolean isExpired()
    {
        return System.nanoTime() >= endTime;
    }

    @Override
    public long timeLeft()
    {
        return TimeUnit.NANOSECONDS.toSeconds(endTime - System.nanoTime());
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeLong(endTime);
    }

    public static RealTime decode(FriendlyByteBuf friendlyByteBuf)
    {
        long expiryMillis = friendlyByteBuf.readLong();
        return new RealTime(expiryMillis);
    }
}
