package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.TrackedJourney;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

/*
Same as PinEntry but timed.
 */
public class TimedPin extends PinEntry
{
    public static final String PIN_ID = "timed_pin";
    public static final Codec<TimedPin> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("marker").forGetter(PinEntry::getMarker),
                    Codec.STRING.fieldOf("level").forGetter(PinEntry::getLevel),
                    Vec3.CODEC.fieldOf("position").forGetter(PinEntry::getPosition),
                    Codec.INT.fieldOf("colour").forGetter(PinEntry::getHighlightColor),
                    EntrySettings.CODEC.fieldOf("settings").forGetter(PinEntry::getSettings),
                    TrackedJourney.CODEC.optionalFieldOf("tracked", TrackedJourney.none()).forGetter(PinEntry::getJourney),
                    TimeKeeper.CODEC.fieldOf("timeKeeper").forGetter(TimedPin::getTimeMethod)
            ).apply(instance, TimedPin::new)
    );

    protected @NotNull TimeKeeper timeMethod;

    public TimedPin(String marker, String level, Vec3 position, Integer colour, EntrySettings settings, TrackedJourney trackedJourney, @NonNull TimeKeeper timeKeeper)
    {
        super(marker, level, position, colour, settings, trackedJourney);
        this.timeMethod = timeKeeper;
    }

    @Override
    public String getExtraInformation()
    {
        long timeLeftMillis = timeMethod.timeLeft();
        return " %sd %sh %sm".formatted(TimeUnit.MILLISECONDS.toDays(timeLeftMillis), TimeUnit.MILLISECONDS.toHours(timeLeftMillis), TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis));
    }

    @Override
    public boolean hasExpired()
    {
        return timeMethod.isExpired();
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        this.timeMethod.tick();
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

    public @NotNull TimeKeeper getTimeMethod()
    {
        return timeMethod;
    }

    @Override
    public String debugString()
    {
        return super.debugString() + " timeMethod=%s, timeLeft=%s".formatted(timeMethod.getKey(), timeMethod.timeLeft());
    }
}
