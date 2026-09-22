package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.DefaultCompassProviderEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.DefaultTimedCompassProvider;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;

public class DeathpointTimedProvider extends DefaultTimedCompassProvider implements Singleton
{
    private static int RED = ColorHelper.rgb(255, 0, 0, 255);

    public DeathpointTimedProvider(LocalPlayer localPlayer)
    {
        super(TimeUnit.MINUTES, 30);
        this.addEntry(DefaultCompassProviderEntry.of(localPlayer.position(), "☠"));
    }

    @Override
    public int getColor()
    {
        return RED;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    @Override
    public boolean isAbsolute()
    {
        return true;
    }
}
