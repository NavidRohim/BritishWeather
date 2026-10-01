package me.brynview.navidrohim.util;

import net.minecraft.world.phys.Vec3;

public class MathHelper
{
    public static int getDistance(Vec3 currentPos, Vec3 distantPos)
    {
        Vec3 relativePos = distantPos.subtract(currentPos);
        // Pythagoras
        return (int) Math.sqrt((relativePos.x*relativePos.x + relativePos.z*relativePos.z));
    }

    public static double angleFromPos(Vec3 pos, Vec3 ourPos) {
        Vec3 relativePos = pos.subtract(ourPos);
        return Math.toDegrees(-Math.atan2(relativePos.x, relativePos.z));
    }
}
