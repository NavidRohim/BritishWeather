package me.brynview.navidrohim.client.hud.compass.entry.impl.animations;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.EntryAnimation;
import net.minecraft.util.Util;

import java.util.function.Consumer;

public class HighlightedAnimation implements EntryAnimation
{

    private static final float ANIMATION_DURATION = 1.0F; // seconds
    private static final double ANIMATION_STEEPNESS = 2.0; // higher = snappier start, longer tail

    private float totalAnimationTime = 0.0F;
    private long oldNow = 0;
    private boolean hasFinished = false;

    private final Consumer<Double> lerpChangeCallback;
    private final AbstractEntry owner;

    public HighlightedAnimation(Consumer<Double> lerpChangeCallback, AbstractEntry owner)
    {
        this.lerpChangeCallback = lerpChangeCallback;
        this.owner = owner;
    }

    @Override
    public AbstractEntry getEntryOwner()
    {
        return owner;
    }

    @Override
    public Consumer<Double> lerpChangeCallback()
    {
        return lerpChangeCallback;
    }

    @Override
    public boolean hasFinished()
    {
        return hasFinished;
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
        float percentageFinished = Math.clamp(totalAnimationTime / ANIMATION_DURATION, 0.0F, 1.0F);
        double animationProgression = (1.0 - Math.pow(2.0, -ANIMATION_STEEPNESS * percentageFinished)) / (1.0 - Math.pow(2.0, -ANIMATION_STEEPNESS));
        this.lerpChangeCallback().accept(animationProgression);

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
        hasFinished = true;
    }
}
