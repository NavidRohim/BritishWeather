package me.brynview.navidrohim.util;

import net.minecraft.world.phys.Vec3;

/*
Helpers for the compass
 */
public class MathHelper
{
    public static int getDistance(Vec3 currentPos, Vec3 distantPos)
    {
        Vec3 relativePos = distantPos.subtract(currentPos);
        // Pythagoras
        return (int) Math.sqrt((relativePos.x*relativePos.x + relativePos.z*relativePos.z));
    }

    /*
    Get angle from current position to a distant position
     */
    public static double angleFromPos(Vec3 pos, Vec3 ourPos) {
        Vec3 relativePos = pos.subtract(ourPos);
        // Use atan2 as it supports a full 360 circle instead of just 180deg
        return Math.toDegrees(-Math.atan2(relativePos.x, relativePos.z));
    }
}
