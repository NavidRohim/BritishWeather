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
    public static final Codec<PinEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("marker").forGetter(PinEntry::getMarker),
                    Codec.STRING.fieldOf("level").forGetter(PinEntry::getLevel),
                    Vec3.CODEC.fieldOf("position").forGetter(PinEntry::getPosition),
                    Codec.INT.fieldOf("colour").forGetter(PinEntry::getHighlightColor),
                    Codec.BOOL.fieldOf("persistent").forGetter(PinEntry::isPersistent),
                    Codec.BOOL.fieldOf("showDistance").forGetter(PinEntry::shouldShowDistance),
                    Codec.BOOL.fieldOf("disappearWhenOutOfBounds").forGetter(PinEntry::shouldDisappearWhenNotInView),
                    Codec.BOOL.optionalFieldOf("showBackground", true).forGetter(PinEntry::shouldShowBackground)
            ).apply(instance, PinEntry::new)
    );

    protected boolean persistent;

    protected boolean shouldShowDistance;
    protected boolean shouldDisappearWhenNotInView;
    protected boolean shouldShowBackground;

    protected EntrySettings settings;

    public PinEntry(String marker, String level, Vec3 position, Integer colour, Boolean persistent, Boolean showDistance, Boolean disappearWhenOutOfBounds, Boolean showBackground)
    {
        super(position, level, marker, colour);
        this.renderer = new PinRenderer(this);

        this.persistent = persistent;
        this.shouldShowDistance = showDistance;
        this.shouldDisappearWhenNotInView = disappearWhenOutOfBounds;
        this.shouldShowBackground = showBackground;
    }

    public EntrySettings getSettings()
    {
        return settings;
    }

    public boolean canBeEdited()
    {
        return true;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    @Override
    public boolean shouldShowDistance()
    {
        return shouldShowDistance;
    }

    public void setShouldShowDistance(boolean shouldShowDistance)
    {
        this.shouldShowDistance = shouldShowDistance;
    }

    @Override
    public boolean shouldDisappearWhenNotInView()
    {
        return shouldDisappearWhenNotInView;
    }

    public void setShouldDisappearWhenNotInView(boolean shouldDisappearWhenNotInView) {this.shouldDisappearWhenNotInView = shouldDisappearWhenNotInView;}

    public boolean shouldShowBackground()
    {
        return shouldShowBackground;
    }

    public void setShouldShowBackground(boolean value)
    {
        this.shouldShowBackground = value;
    }

    @Override
    public String getType()
    {
        return "pin";
    }

    public Tag encode()
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }
}
