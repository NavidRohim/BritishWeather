package me.brynview.navidrohim.util;

import java.awt.*;

public class ColorHelper {

    // Colours for compass elements
    public static final int CENTER_COLOR = decode("#FF0000").getRGB(); // Red
    public static final int COMPASS_BG_COLOR = rgb(255, 255, 255, 190);
    public static final int COMPASS_BG_SHADOW_COLOUR = rgb(0, 0, 0, 100);
    public static final int WHITE = decode("#FFFFFF").getRGB();
    // Colours for entity markers
    public static final int PLAYER = rgb(125, 255, 125, 255);
    public static final int NEUTRAL = rgb(255, 255, 255, 255);
    public static final int HOSTILE = rgb(255, 125, 125, 255);
    public static final int ALLY = rgb(125, 125, 255, 255);

    public static int rgb(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static Color decode(String color) {
        return Color.decode(color);
    }
}
