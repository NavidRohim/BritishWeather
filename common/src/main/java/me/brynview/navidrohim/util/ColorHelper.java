package me.brynview.navidrohim.util;

import net.minecraft.util.ARGB;

import java.awt.*;

public class ColorHelper {

    // Colours for compass elements
    public static final int CENTER_COLOR = decode("#FF0A01").getRGB(); // Red
    public static final int COMPASS_BG_COLOR = decode("#FFFFFF").getRGB();
    public static final int COMPASS_BG_SHADOW_COLOUR = ARGB.scaleRGB(COMPASS_BG_COLOR, 0.25f);

    public static final int WHITE = decode("#FFFFFF").getRGB();
    // Colours for entity markers
    public static final int PLAYER = rgb(125, 255, 125, 255);
    public static final int NEUTRAL = rgb(255, 255, 255, 255);
    public static final int HOSTILE = rgb(255, 125, 125, 255);

    public static int rgb(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static Color decode(String color) {
        return Color.decode(color);
    }
}
