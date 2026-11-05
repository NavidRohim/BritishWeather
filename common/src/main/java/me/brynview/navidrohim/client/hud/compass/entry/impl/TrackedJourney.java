package me.brynview.navidrohim.client.hud.compass.entry.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.util.Mth;
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

    public double getJourneyProgress(Vec3 current) {
        if (MathHelper.getDistance(current, end) == 0)
        {
            return 100.0;
        }

        double dx = end.x - start.x;
        double dz = end.z - start.z;

        double lengthSquared = dx * dx + dz * dz;

        double px = current.x - start.x;
        double pz = current.z - start.z;

        double dot = px * dx + pz * dz;

        double progress = dot / lengthSquared;

        if (progress > 1.0) {
            progress = Math.max(0.0, 2.0 - progress);
        } else {
            if  (progress < 0.0)
            {
                return -1;
            }
            progress = Math.max(0.0, progress);
        }

        return progress * 100.0;
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

    public static TrackedJourney none()
    {
        return new TrackedJourney(true, null, null);
    }
}
