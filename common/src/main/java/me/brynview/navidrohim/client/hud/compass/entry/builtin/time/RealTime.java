package me.brynview.navidrohim.client.hud.compass.entry.builtin.time;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.TimeMethod;
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
        friendlyByteBuf.writeUtf(getKey().name());
        friendlyByteBuf.writeLong(endTime);
    }

    @Override
    public TimeMethods getKey()
    {
        return TimeMethod.TimeMethods.REALTIME;
    }

    public static RealTime decode(FriendlyByteBuf friendlyByteBuf)
    {
        long expiryMillis = friendlyByteBuf.readLong();
        return new RealTime(expiryMillis);
    }
}
