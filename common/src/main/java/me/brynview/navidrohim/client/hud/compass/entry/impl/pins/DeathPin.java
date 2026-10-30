package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.CharEntryRenderer;
import me.brynview.navidrohim.client.hud.compass.entry.time.GameTime;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/*
Entry that will be added upon user death. This is a timed entry and lasts for 30 minutes IRL.
Time offline will count towards the timer.
 */
public final class DeathPin extends TimedPin
{
    private static final String SKULL = "☠";
    private boolean hasArrived = false; // If the user has arrived at the deathpoint.
    private final Vec3i deathpoint;

    /*
    Normal constructor
     */
    public DeathPin(Vec3 position, String level)
    {
        super(position, level, SKULL, new GameTime(TimeUnit.MINUTES, 30), true, ColorHelper.RED, true, false);
        this.deathpoint = new Vec3i((int) position.x, (int) position.y, (int) position.z);
        this.renderer = new CharEntryRenderer(this);

    }

    /*
    Constructor if being deserialized from NBT.
     */
    public DeathPin(String ignoredMarker, int ignored, Vec3 pos, String level, FriendlyByteBuf friendlyByteBuf)
    {
        super(SKULL, ColorHelper.RED, pos, level, friendlyByteBuf);
        this.deathpoint = new Vec3i((int) pos.x, (int) pos.y, (int) pos.z);
        this.renderer = new CharEntryRenderer(this);
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        super.tick(player);
        this.timeMethod.tick();

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
        return this.timeMethod.isExpired() || hasArrived;
    }

    @Override
    public boolean isPersistent()
    {
        // Persistent through game sessions
        return true;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    @Override
    public int getHighlightColor()
    {
        return ColorHelper.RED;
    }

    @Override
    public boolean canBeEdited()
    {
        return false;
    }
}
