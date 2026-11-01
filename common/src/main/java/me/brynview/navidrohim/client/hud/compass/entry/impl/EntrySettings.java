package me.brynview.navidrohim.client.hud.compass.entry.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class EntrySettings
{
    public static final Codec<EntrySettings> CODEC = RecordCodecBuilder.create(entrySettingsInstance ->
            entrySettingsInstance.group(
                    Codec.BOOL.fieldOf("persistent").forGetter(EntrySettings::isPersistent),
                    Codec.BOOL.fieldOf("showBackground").forGetter(EntrySettings::shouldShowBackground),
                    Codec.BOOL.fieldOf("disappearWhenOutOfBounds").forGetter(EntrySettings::shouldDisappearWhenOOB),
                    Codec.BOOL.fieldOf("showDistance").forGetter(EntrySettings::shouldShowDistance)
            ).apply(entrySettingsInstance, EntrySettings::new)
    );

    private EntrySettings(boolean persistent, boolean showBackground, boolean disappearWhenOOB, boolean showDistance)
    {
        this.isPersistent = persistent;
        this.showBackground = showBackground;
        this.disappearWhenOOB = disappearWhenOOB;
        this.showDistance = showDistance;
    }

    public EntrySettings()
    {}

    private boolean isPersistent = true;
    private boolean showBackground = true;
    private boolean disappearWhenOOB = true;
    private boolean showDistance = false;

    public boolean isPersistent()
    {
        return isPersistent;
    }

    public EntrySettings setPersistent(boolean persistent)
    {
        isPersistent = persistent;
        return this;
    }

    public boolean shouldShowBackground()
    {
        return showBackground;
    }

    public EntrySettings setShowBackground(boolean showBackground)
    {
        this.showBackground = showBackground;
        return this;
    }

    public boolean shouldDisappearWhenOOB()
    {
        return disappearWhenOOB;
    }

    public EntrySettings setDisappearWhenOOB(boolean disappearWhenOOB)
    {
        this.disappearWhenOOB = disappearWhenOOB;
        return this;
    }

    public boolean shouldShowDistance()
    {
        return showDistance;
    }

    public EntrySettings setShowDistance(boolean showDistance)
    {
        this.showDistance = showDistance;
        return this;
    }

    public static EntrySettings defaults()
    {
        return new EntrySettings(true, true, true, false);
    }


}
