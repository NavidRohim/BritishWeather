package me.brynview.navidrohim.client.hud.compass.entry.impl.time;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;

import java.util.concurrent.TimeUnit;

public class RealTime extends TimeKeeper
{
    public static final MapCodec<RealTime> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.LONG.fieldOf("endTime").forGetter(RealTime::getEndTime)
            ).apply(instance, RealTime::new)
    );

    private final long endTime;

    public RealTime(TimeUnit unit, int duration)
    {
        this.endTime = System.currentTimeMillis() + unit.toMillis(duration);
    }

    public RealTime(long remaining)
    {
        this.endTime = remaining;
    }

    @Override
    public boolean isExpired()
    {
        return System.currentTimeMillis() >= endTime;
    }

    @Override
    public long timeLeft()
    {
        return endTime - System.currentTimeMillis();
    }

    public long getEndTime()
    {
        return endTime;
    }

    @Override
    public String getKey()
    {
        return "realtime";
    }
}
