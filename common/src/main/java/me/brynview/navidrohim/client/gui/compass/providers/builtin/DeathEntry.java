package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultTimedEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class DeathEntry extends DefaultTimedEntry implements Singleton
{
    private static final int RED = ColorHelper.rgb(255, 0, 0, 255);
    private static final String SKULL = "☠";

    private boolean hasArrived = false;
    private final Vec3i deathpoint;

    public DeathEntry(Vec3 position)
    {
        super(position, SKULL, TimeUnit.MINUTES, 30);
        this.deathpoint = new Vec3i((int) position.x, (int) position.y, (int) position.z);
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        Vec3 playerPos = player.position();
        if ( !player.isDeadOrDying() && (int) playerPos.x == deathpoint.getX() && (int) Math.abs(playerPos.y - deathpoint.getY()) <= 4 && (int) playerPos.z == deathpoint.getZ())
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
    public int getColour()
    {
        return RED;
    }
}
