package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import me.brynview.navidrohim.Constants;

import java.util.concurrent.TimeUnit;

public class DefaultTimedCompassProvider extends DefaultCompassProvider
{
    private long startTimeNano;

    public DefaultTimedCompassProvider(TimeUnit unit, long duration)
    {
        startTimeNano = System.nanoTime() + unit.toNanos(duration);
    }

    @Override
    public boolean hasExpired()
    {
        return System.nanoTime() >= startTimeNano;
    }
}
