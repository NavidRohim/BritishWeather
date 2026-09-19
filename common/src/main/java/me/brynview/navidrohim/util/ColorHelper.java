package me.brynview.navidrohim.util;

import java.awt.*;

public class ColorHelper {

    public static int rgb(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static Color decode(String color) {
        return Color.decode(color);
    }
}
