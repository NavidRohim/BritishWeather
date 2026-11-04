package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.TrackedJourney;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public class PinEntry extends AbstractEntry
{
    public static final String PIN_ID = "pin";
    public static final Codec<PinEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("marker").forGetter(PinEntry::getMarker),
                    Codec.STRING.fieldOf("level").forGetter(PinEntry::getLevel),
                    Vec3.CODEC.fieldOf("position").forGetter(PinEntry::getPosition),
                    Codec.INT.fieldOf("colour").forGetter(PinEntry::getHighlightColor),
                    EntrySettings.CODEC.fieldOf("settings").forGetter(PinEntry::getSettings),
                    TrackedJourney.CODEC.optionalFieldOf("tracked", TrackedJourney.empty()).forGetter(PinEntry::getJourney)
            ).apply(instance, PinEntry::new)
    );

    public TrackedJourney trackedJourney;

    public PinEntry(String marker, String level, Vec3 position, Integer colour, EntrySettings settings, TrackedJourney tracked)
    {
        super(position, level, marker, colour, settings);
        this.trackedJourney = tracked;
        this.renderer = new EntryRenderer<>(this);
    }

    @Override
    public String getType()
    {
        return PIN_ID;
    }

    public Tag encode()
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public boolean canBeEdited()
    {
        return true;
    }

    @Override
    public boolean isTracked()
    {
        return this.trackedJourney.tracked;
    }

    @Override
    public void track(Vec3 position)
    {
        this.trackedJourney.tracked = true;
        this.trackedJourney.setStartPosition(position);
        this.trackedJourney.setEndPosition(this.getPosition());

    }

    @Override
    public void untrack()
    {
        this.trackedJourney.reset();
    }

    public TrackedJourney getJourney()
    {
        return trackedJourney;
    }
}
