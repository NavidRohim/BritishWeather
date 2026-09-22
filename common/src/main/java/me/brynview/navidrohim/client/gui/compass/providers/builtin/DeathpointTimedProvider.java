package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.DefaultCompassProviderEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.DefaultTimedCompassProvider;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DeathpointTimedProvider extends DefaultTimedCompassProvider implements Singleton
{
    private static int RED = ColorHelper.rgb(255, 0, 0, 255);

    private final Vec3i deathpoint;
    private boolean hasArrived = false;

    public DeathpointTimedProvider(LocalPlayer localPlayer)
    {
        super(TimeUnit.MINUTES, 30);

        Vec3 dp = localPlayer.position();
        this.deathpoint = new Vec3i((int) dp.x, (int) dp.y, (int) dp.z);
        this.addEntry(DefaultCompassProviderEntry.of(dp, "☠"));
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        Vec3 playerPos = player.position();
        if ((int) playerPos.x == deathpoint.getX() && (int) playerPos.y == deathpoint.getY() && (int) playerPos.z == deathpoint.getZ() && !player.isDeadOrDying())
        {
            hasArrived = true;
        }
    }

    @Override
    public boolean hasExpired()
    {
        return super.hasExpired() || hasArrived;
    }

    @Override
    public int getColor()
    {
        return RED;
    }

    @Override
    public boolean isAbsolute()
    {
        return true;
    }
}
