package me.brynview.navidrohim.util;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class MathHelper
{

    public static double angleFromPos(Vec3 pos, Vec3 ourPos) {
        Vec3 relativePos = pos.subtract(ourPos);
        return Math.toDegrees(-Math.atan2(relativePos.x, relativePos.z));
    }

    public static float angleDistance(float yaw, float angle) {
        float dist = angle - yaw;

        if (dist > 0) {
            return dist > 180 ? (dist - 360) : dist;
        }

        return dist < -180 ? (dist + 360) : dist;
    }

    public static int getCompassScreenX(float yaw, float angle, int x, float offset) {
        // Text is centered at the center of the screen. aDist is used as an offset
        // Return an int between -180 and +180 (360)
        // yaw: float between 0.0 and 360.0
        // angle: float between 0 and 270
        // aDist example: (yaw = 90, angle = 0) aDist = angle - yaw = -90
        int aDist = (int) angleDistance(yaw, angle);

        // Make sure text is not rendered past the compass bounds
        if (Math.abs(aDist) <= (90 * offset)) {
            return x + aDist; // x will be center of the screen. aDist is the offset where to render text
        }

        // Return max int to make sure whatever is going funny fucks off into the dirt
        return Integer.MAX_VALUE;
    }

    public static int getCompassX(int screenWidth, int textWidth) {
        return (screenWidth - textWidth) / 2;
    }

    public static int getCompassY() {
        return 2;
    }

    public static int getDistance(Vec3 currentPos, Vec3 distantPos)
    {
        Vec3 relativePos = distantPos.subtract(currentPos);
        // Pythagoras
        return (int) Math.sqrt((relativePos.x*relativePos.x + relativePos.z*relativePos.z));
    }
}
