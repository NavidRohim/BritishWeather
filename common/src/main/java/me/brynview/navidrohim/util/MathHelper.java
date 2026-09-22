package me.brynview.navidrohim.util;

import me.brynview.navidrohim.Constants;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MathHelper
{
    public static int getCompassScreenX(float yaw, float angle, int x, int compassScaledWidth, boolean shouldDisappearWhenOOB) {
        // Text is centered at the center of the screen. aDist is used as an offset
        // Return an int between -180 and +180 (360)
        // yaw: float between 0.0 and 360.0
        // angle: float between 0 and 270
        // aDist example: (yaw = 90, angle = 0) aDist = angle - yaw = -90

        int compassScaledWidthHalf = compassScaledWidth / 2;
        int aDist = (int) (Mth.wrapDegrees(angle - yaw) * ((float) compassScaledWidth / 180));
        int absADist = Math.abs(aDist);
        int csx = x + aDist;

        // Make sure text is not rendered past the compass bounds

        if (absADist < compassScaledWidthHalf) {
            return csx; // x will be center of the screen. aDist is the offset where to render text
        }

        if (!shouldDisappearWhenOOB && (aDist >= compassScaledWidthHalf))
        {
            return x + compassScaledWidthHalf;
        } else if (!shouldDisappearWhenOOB && aDist <= -compassScaledWidthHalf)
        {
            return x - compassScaledWidthHalf;
        }

        // Return max int to make sure whatever is going out of bounds fucks right off so we don't see it
        return Integer.MAX_VALUE;
    }

    public static int getCompassScreenX(float yaw, float angle, int x, int compassScaledWidth)
    {
        return getCompassScreenX(yaw, angle, x, compassScaledWidth, true);
    }

    public static boolean isXOutOfBounds(int x, int compassScaledWidthHalf, int compassX)
    {
        return x <= compassX - compassScaledWidthHalf || x >= compassX + compassScaledWidthHalf;
    }

    public static int getCompassX(int screenWidth, int textWidth) {
        return (screenWidth - textWidth) / 2;
    }

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
