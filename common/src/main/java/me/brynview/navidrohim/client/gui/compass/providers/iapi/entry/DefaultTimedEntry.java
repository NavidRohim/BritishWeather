package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;

public abstract class DefaultTimedEntry extends DefaultEntry
{
    private final long startTime;
    private final long endTime;

    public DefaultTimedEntry(Vec3 position, String marker, TimeUnit unit, int duration)
    {
        super(position, marker);
        startTime = System.nanoTime();
        endTime = startTime + unit.toNanos(duration);
    }

    @Override
    public boolean hasExpired()
    {
        return System.nanoTime() > endTime;
    }
}
