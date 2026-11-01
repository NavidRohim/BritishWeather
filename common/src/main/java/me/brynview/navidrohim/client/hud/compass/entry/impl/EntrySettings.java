package me.brynview.navidrohim.client.hud.compass.entry.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class EntrySettings
{
    public static final Codec<EntrySettings> CODEC = RecordCodecBuilder.create(entrySettingsInstance ->
            entrySettingsInstance.group(
                    Codec.BOOL.fieldOf("persistent").forGetter(EntrySettings::isPersistent),
                    Codec.BOOL.fieldOf("showBackground").forGetter(EntrySettings::isShowBackground),
                    Codec.BOOL.fieldOf("disappearWhenOutOfBounds").forGetter(EntrySettings::isDisappearWhenOOB),
                    Codec.BOOL.fieldOf("showDistance").forGetter(EntrySettings::isShowDistance)
            ).apply(entrySettingsInstance, EntrySettings::new)
    );

    private EntrySettings(boolean persistent, boolean showBackground, boolean disappearWhenOOB, boolean showDistance)
    {
        this.isPersistent = persistent;
        this.showBackground = showBackground;
        this.disappearWhenOOB = disappearWhenOOB;
        this.showDistance = showDistance;
    }

    private boolean isPersistent = true;
    private boolean showBackground = true;
    private boolean disappearWhenOOB = true;
    private boolean showDistance = false;

    public boolean isPersistent()
    {
        return isPersistent;
    }

    public void setPersistent(boolean persistent)
    {
        isPersistent = persistent;
    }

    public boolean isShowBackground()
    {
        return showBackground;
    }

    public void setShowBackground(boolean showBackground)
    {
        this.showBackground = showBackground;
    }

    public boolean isDisappearWhenOOB()
    {
        return disappearWhenOOB;
    }

    public void setDisappearWhenOOB(boolean disappearWhenOOB)
    {
        this.disappearWhenOOB = disappearWhenOOB;
    }

    public boolean isShowDistance()
    {
        return showDistance;
    }

    public void setShowDistance(boolean showDistance)
    {
        this.showDistance = showDistance;
    }

    public static EntrySettings defaults()
    {
        return new EntrySettings(true, true, true, false);
    }
}
