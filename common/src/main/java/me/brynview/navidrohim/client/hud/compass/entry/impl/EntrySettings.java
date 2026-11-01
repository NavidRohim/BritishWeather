package me.brynview.navidrohim.client.hud.compass.entry.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class EntrySettings
{
    public static final boolean SHOW_SHADOWS_DEFAULT = false;
    public static final boolean PERSISTENT_DEFAULT = true;
    public static final boolean SHOW_BACKGROUND_DEFAULT = true;
    public static final boolean SHOW_DISTANCE_DEFAULT = false;
    public static final boolean SHOULD_DISAPPEAR_OOB = true;

    public static final Codec<EntrySettings> CODEC = RecordCodecBuilder.create(entrySettingsInstance ->
            entrySettingsInstance.group(
                    Codec.BOOL.fieldOf("persistent").forGetter(EntrySettings::isPersistent),
                    Codec.BOOL.fieldOf("showBackground").forGetter(EntrySettings::shouldShowBackground),
                    Codec.BOOL.fieldOf("disappearWhenOutOfBounds").forGetter(EntrySettings::shouldDisappearWhenOOB),
                    Codec.BOOL.fieldOf("showDistance").forGetter(EntrySettings::shouldShowDistance),
                    Codec.BOOL.optionalFieldOf("showShadow", SHOW_SHADOWS_DEFAULT).forGetter(EntrySettings::shouldShowShadow)
            ).apply(entrySettingsInstance, EntrySettings::new)
    );

    private EntrySettings(boolean persistent, boolean showBackground, boolean disappearWhenOOB, boolean showDistance, boolean showShadow)
    {
        this.isPersistent = persistent;
        this.showBackground = showBackground;
        this.disappearWhenOOB = disappearWhenOOB;
        this.showDistance = showDistance;
        this.showShadow = showShadow;
    }

    public EntrySettings()
    {
    }

    private boolean isPersistent = PERSISTENT_DEFAULT;
    private boolean showBackground = SHOW_BACKGROUND_DEFAULT;
    private boolean disappearWhenOOB = SHOULD_DISAPPEAR_OOB;
    private boolean showDistance = SHOW_DISTANCE_DEFAULT;
    private boolean showShadow = SHOW_SHADOWS_DEFAULT;

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

    public boolean shouldShowShadow()
    {
        return showShadow;
    }

    public EntrySettings setShadow(boolean showShadow)
    {
        this.showShadow = showShadow;
        return this;
    }

    public static EntrySettings defaults()
    {
        return new EntrySettings(PERSISTENT_DEFAULT, SHOW_BACKGROUND_DEFAULT, SHOULD_DISAPPEAR_OOB, SHOW_DISTANCE_DEFAULT, SHOW_SHADOWS_DEFAULT);
    }


}
