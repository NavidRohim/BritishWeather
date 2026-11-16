package me.brynview.navidrohim.client.hud.compass.entry.impl.animations;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.EntryAnimation;
import net.minecraft.util.Util;

import java.util.function.Consumer;

public class FadeoutAnimation implements EntryAnimation
{

    private static final float ANIMATION_DURATION = 0.4F;

    private final Consumer<Double> lerpChangeCallback;
    private final AbstractEntry owner;

    private float totalAnimationTime = 0.0F;
    private long oldNow = 0;

    public FadeoutAnimation(Consumer<Double> lerpChangeCallback, AbstractEntry owner)
    {
        this.lerpChangeCallback = lerpChangeCallback;
        this.owner = owner;
    }

    @Override
    public AbstractEntry getEntryOwner()
    {
        return this.owner;
    }

    @Override
    public Consumer<Double> lerpChangeCallback()
    {
        return this.lerpChangeCallback;
    }

    @Override
    public void tick()
    {
        long now = Util.getNanos();
        totalAnimationTime += oldNow <= 0 ? 0.0F : (now - oldNow) / Constants.NANOS_TO_SECONDS_MAGIC;
        float remainingTime = ANIMATION_DURATION - totalAnimationTime;
        oldNow = now;

        // Exponential ease-out: fast at the start, slowing as it approaches 1.
        // Normalised by 1 - 2^-k so it lands exactly on 1 at the end of the duration.
        double percentageFinished = Math.clamp(totalAnimationTime / ANIMATION_DURATION, 0.0F, 1.0F);
        this.lerpChangeCallback().accept(percentageFinished);

        if (remainingTime < 0.01F)
        {
            reset();
        }
    }

    @Override
    public void reset()
    {
        oldNow = 0;
        totalAnimationTime = 0;
    }
}
