package me.brynview.navidrohim.client.hud.compass.entry.impl.time;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;

import java.util.concurrent.TimeUnit;

public class GameTime extends TimeKeeper
{
    // GameTime uses ticks instead of nanos like RealTime.

    public static final MapCodec<GameTime> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("currentTick").forGetter(GameTime::getCurrentTick),
                    Codec.LONG.fieldOf("endTick").forGetter(GameTime::getEndTick)
            ).apply(instance, GameTime::new)
    );

    public int currentTick;
    public long endTick;

    public GameTime(int currentTick, long endTick)
    {
        this.currentTick = currentTick;
        this.endTick = endTick;
    }

    /*
    Deserialisation constructor
     */
    public GameTime(TimeUnit unit, long duration)
    {
        currentTick = 0;
        endTick = unit.toSeconds(duration) * 20;
    }

    @Override
    public void tick()
    {
        currentTick++;
    }

    @Override
    public boolean isExpired()
    {
        return currentTick >= endTick;
    }

    @Override
    public long timeLeft()
    {
        // timeLeft return value should be in millis
        return (endTick - currentTick) * 50L;
    }

    @Override
    public String getKey()
    {
        return "gametime";
    }

    public int getCurrentTick()
    {
        return currentTick;
    }

    public long getEndTick()
    {
        return endTick;
    }
}
