package me.brynview.navidrohim.client.hud.compass.entry.builtin.time;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.TimeMethod;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.TimeUnit;

public class GameTime implements TimeMethod
{
    // GameTime uses ticks instead of nanos like RealTime.
    public int currentTick;
    public int endTick;

    public GameTime(int currentTick, int endTick)
    {
        this.currentTick = currentTick;
        this.endTick = endTick;
    }

    /*
    Deserialisation constructor
     */
    public GameTime(TimeUnit unit, int duration)
    {
        currentTick = 0;
        endTick = Math.toIntExact(unit.toSeconds(duration) * 20);
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
        // timeLeft return value should be in seconds.
        return (endTick - currentTick) / 20;
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeUtf(getKey().name());
        friendlyByteBuf.writeInt(endTick);
        friendlyByteBuf.writeInt(currentTick);
    }

    @Override
    public TimeMethods getKey()
    {
        return TimeMethod.TimeMethods.GAMETIME;
    }

    public static GameTime decode(FriendlyByteBuf friendlyByteBuf)
    {
        int endTick = friendlyByteBuf.readInt();
        int lastTick = friendlyByteBuf.readInt();

        return new GameTime(lastTick, endTick);
    }
}
