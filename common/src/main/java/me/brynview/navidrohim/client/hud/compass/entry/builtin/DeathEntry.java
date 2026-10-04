package me.brynview.navidrohim.client.hud.compass.entry.builtin;

import me.brynview.navidrohim.client.hud.compass.entry.builtin.time.RealTime;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultTimedEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.Singleton;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/*
Entry that will be added upon user death. This is a timed entry and lasts for 30 minutes IRL.
Time offline will count towards the timer.
 */
public final class DeathEntry extends DefaultTimedEntry implements Singleton
{
    private static final int RED = ColorHelper.rgb(255, 0, 0, 255);
    private static final String SKULL = "☠";

    private boolean hasArrived = false; // If the user has arrived at the deathpoint.
    private final Vec3i deathpoint;

    /*
    Normal constructor
     */
    public DeathEntry(Vec3 position, String level)
    {
        super(position, level, SKULL, new RealTime(TimeUnit.MINUTES, 30));
        this.deathpoint = new Vec3i((int) position.x, (int) position.y, (int) position.z);
    }

    /*
    Constructor if being deserialized from NBT.
     */
    public DeathEntry(String marker, Vec3 pos, String level, FriendlyByteBuf friendlyByteBuf)
    {
        RealTime realTime = RealTime.decode(friendlyByteBuf); // Decode the RealTime instance.
        super(pos, level, marker, realTime);
        this.deathpoint = new Vec3i((int) pos.x, (int) pos.y, (int) pos.z);
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        Vec3 playerPos = player.position();

        // Checking if the player is dead is important.
        // When the user dies, they will still technically have "arrived" at the deathpoint as their location stays the same until they respawn.
        if ( !player.isDeadOrDying() && (int) playerPos.x == deathpoint.getX() && (int) Math.abs(playerPos.y - deathpoint.getY()) <= 4 && (int) playerPos.z == deathpoint.getZ())
        {
            hasArrived = true;
        }
    }

    @Override
    public boolean hasExpired()
    {
        // entry will expire if player arrives at their deathpoint.
        return super.hasExpired() || hasArrived;
    }

    @Override
    public boolean isPersistent()
    {
        // Persistent through game sessions
        return true;
    }

    @Override
    public int getColour()
    {
        return RED;
    }
}
