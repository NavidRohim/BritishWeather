package me.brynview.navidrohim.client.hud.compass.entry.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public class TrackedJourney
{
    public static final Codec<TrackedJourney> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("tracked").forGetter(TrackedJourney::isTracked),
                    Vec3.CODEC.fieldOf("start").forGetter(TrackedJourney::getStartPosition),
                    Vec3.CODEC.fieldOf("end").forGetter(TrackedJourney::getEndPosition)
            ).apply(instance, TrackedJourney::new)
    );

    public boolean tracked;
    public Vec3 start;
    public Vec3 end;

    private TrackedJourney(boolean tracked, Vec3 start, Vec3 end)
    {
        this.tracked = tracked;
        this.start = start;
        this.end = end;
    }

    public boolean isTracked()
    {
        return tracked;
    }

    public Vec3 getStartPosition()
    {
        return start;
    }

    public Vec3 getEndPosition()
    {
        return end;
    }

    public void setStartPosition(Vec3 start)
    {
        this.start = start;
    }

    public void setEndPosition(Vec3 end)
    {
        this.end = end;
    }

    public int getProgressFromStart(Vec3 position)
    {
        double dx = end.x - start.x;
        double dz = end.z - start.z;

        double totalLengthSquared = dx * dx + dz * dz;

        if (totalLengthSquared == 0) {
            return 0;
        }

        double progress = ((position.x - start.x) * dx + (position.z - start.z) * dz) / totalLengthSquared;
        return (int) Math.clamp(progress * 100.0, 0.0, 101.0);
    }

    public void reset()
    {
        this.tracked = false;
        this.start = Vec3.ZERO;
        this.end = Vec3.ZERO;
    }

    public static TrackedJourney empty()
    {
        return new TrackedJourney(false, Vec3.ZERO, Vec3.ZERO);
    }
}
