package me.brynview.navidrohim.util;

import me.brynview.navidrohim.Constants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ColorHelper {

    // Colours for compass elements
    public static final int CENTER_COLOR = decode("#FF0A01").getRGB(); // Red
    public static final int COMPASS_BG_COLOR = decode("#FFFFFF").getRGB();
    public static final int COMPASS_BG_SHADOW_COLOUR = ARGB.scaleRGB(COMPASS_BG_COLOR, 0.25f);

    public static final int WHITE = decode("#FFFFFF").getRGB();
    public static final int RED = decode("#FF0000").getRGB();
    public static final int GREEN = decode("#00FF00").getRGB();
    public static final int BLUE = decode("#0000FF").getRGB();

    // Colours for entity markers
    public static final int PLAYER = rgb(125, 255, 125, 255);
    public static final int NEUTRAL = rgb(255, 255, 255, 255);
    public static final int HOSTILE = rgb(255, 125, 125, 255);
    public static final int ALLY = rgb(127, 127, 255, 255);

    public static final int OBJECTIVE_MARKER_COLOUR = rgb(243, 238, 159, 255);

    public static final int BORDER_COLOUR = 0xFF202020;

    public static final int SCALE_MAX = 255; // Max value of what the entity distance scale should be (entity further away = lower, closer = higher)

    public static int rgb(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static Color decode(String color) {
        return Color.decode(color);
    }

    public static int getOpacity(int step, int currentV, boolean invert)
    {
        int percentageOfMax = SCALE_MAX / step * currentV;
        if (invert)
        {
            return Math.abs(SCALE_MAX - percentageOfMax);
        }
        return percentageOfMax;
    }

    public static int shiftColourToOpacity(int colour, int opacity)
    {
        return (colour & 0x00FFFFFF) | (opacity << 24);
    }

    public static int getColourForEntity(LivingEntity entity, @Nullable LocalPlayer owner, int opacity)
    {
        switch (entity)
        {
            case Player _ ->
            {
                return shiftColourToOpacity(PLAYER, opacity);
            }
            case Monster _ ->
            {
                return shiftColourToOpacity(HOSTILE, opacity);
            }
            case OwnableEntity tamableAnimal ->
            {
                if (tamableAnimal.getOwnerReference() != null && tamableAnimal.getOwnerReference().getUUID().equals(owner.getUUID()) ||
                    tamableAnimal instanceof AbstractHorse horseLike && horseLike.isTamed()
                )
                {
                    return shiftColourToOpacity(ALLY, opacity);
                }
            }
            default ->
            {
            }
        }
        return shiftColourToOpacity(NEUTRAL, opacity);
    }
}
