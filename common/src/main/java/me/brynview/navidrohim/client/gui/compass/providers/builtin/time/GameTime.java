package me.brynview.navidrohim.client.gui.compass.providers.builtin.time;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.TimeMethod;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.TimeUnit;

public class GameTime implements TimeMethod
{
    public int currentTick;
    public int endTick;

    public GameTime(int currentTick, int endTick)
    {
        this.currentTick = currentTick;
        this.endTick = endTick;
    }

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
        return (endTick - currentTick) / 20;
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeInt(endTick);
        friendlyByteBuf.writeInt(currentTick);
    }

    public static GameTime decode(FriendlyByteBuf friendlyByteBuf)
    {
        int endTick = friendlyByteBuf.readInt();
        int lastTick = friendlyByteBuf.readInt();

        return new GameTime(lastTick, endTick);
    }
}
