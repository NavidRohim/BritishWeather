package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.PinRenderer;
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
                    EntrySettings.CODEC.fieldOf("settings").forGetter(PinEntry::getSettings)
            ).apply(instance, PinEntry::new)
    );

    protected EntrySettings settings;

    public PinEntry(String marker, String level, Vec3 position, Integer colour, EntrySettings settings)
    {
        super(position, level, marker, colour);
        this.renderer = new PinRenderer(this);
        this.settings = settings;
    }

    @Override
    public String getType()
    {
        return PIN_ID;
    }

    public EntrySettings getSettings()
    {
        return settings;
    }

    public Tag encode()
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public boolean canBeEdited()
    {
        return true;
    }
}
