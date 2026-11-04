package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.TrackedJourney;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.CharEntryRenderer;
import me.brynview.navidrohim.client.hud.compass.entry.impl.time.GameTime;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/*
Entry that will be added upon user death. This is a timed entry and lasts for 30 minutes IRL.
Time offline will count towards the timer.
 */
public final class DeathPin extends TimedPin
{
    public static final String PIN_ID = "death_pin";

    public static final Codec<TimedPin> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("marker").forGetter(PinEntry::getMarker),
                    Codec.STRING.fieldOf("level").forGetter(PinEntry::getLevel),
                    Vec3.CODEC.fieldOf("position").forGetter(PinEntry::getPosition),
                    Codec.INT.fieldOf("colour").forGetter(PinEntry::getHighlightColor),
                    EntrySettings.CODEC.fieldOf("settings").forGetter(PinEntry::getSettings),
                    TrackedJourney.CODEC.optionalFieldOf("tracked", TrackedJourney.empty()).forGetter(PinEntry::getJourney),
                    TimeKeeper.CODEC.fieldOf("timeKeeper").forGetter(TimedPin::getTimeMethod)
            ).apply(instance, DeathPin::new)
    );

    private static final String SKULL = "☠";
    private boolean hasArrived = false; // If the user has arrived at the deathpoint.
    private final Vec3i deathpoint;

    /*
    Normal constructor
     */
    public DeathPin(Vec3 position, String level)
    {
        super(SKULL, level, position, ColorHelper.RED, EntrySettings.defaults().setShowDistance(true), TrackedJourney.empty(), new GameTime(TimeUnit.MINUTES, 30));
        this.deathpoint = new Vec3i((int) position.x, (int) position.y, (int) position.z);
        this.renderer = new CharEntryRenderer(this);
    }

    public DeathPin(String marker, String level, Vec3 position, Integer colour, EntrySettings settings, TrackedJourney trackedJourney, TimeKeeper timeKeeper)
    {
        super(marker, level, position, colour, settings, trackedJourney, timeKeeper);
        this.deathpoint = new Vec3i((int) position.x, (int) position.y, (int) position.z);
        this.renderer = new CharEntryRenderer(this);
    }

    /*
    Constructor if being deserialized from NBT.
     */

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

    @Override
    public String getType()
    {
        return PIN_ID;
    }

    @Override
    public Tag encode()
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }
}
