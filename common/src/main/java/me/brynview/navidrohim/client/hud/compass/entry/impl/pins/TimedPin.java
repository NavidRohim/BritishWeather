package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.TrackedJourney;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

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
        StringBuilder sb = new StringBuilder().append(' ');
        long totalSeconds = getTimeMethod().timeLeft() / 1000L;

        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;

        if (days > 0)
        {
            sb.append(days).append("d ");
        }

        if (hours > 0)
        {
            sb.append(hours).append("h ");
        }

        if (minutes > 0)
        {
            sb.append(minutes).append("m ");
        }

        return sb.toString();
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
