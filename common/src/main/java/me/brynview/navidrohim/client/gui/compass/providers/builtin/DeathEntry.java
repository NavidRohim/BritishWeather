package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultTimedEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;

public final class DeathEntry extends DefaultTimedEntry implements Singleton
{
    private static final int RED = ColorHelper.rgb(255, 0, 0, 255);

    public DeathEntry(Vec3 position)
    {
        super(position, "D", TimeUnit.MINUTES, 30);
    }

    @Override
    public String getMarker()
    {
        return "D";
    }

    @Override
    public int getColour()
    {
        return RED;
    }

    @Override
    public boolean isAbsolute()
    {
        return true;
    }
}
