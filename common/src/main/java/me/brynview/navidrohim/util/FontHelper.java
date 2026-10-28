package me.brynview.navidrohim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FontHelper {

    public static void draw(Minecraft mc, GuiGraphicsExtractor guiGraphics, String label, int x, int y, int color) {
        draw(mc, guiGraphics, label, x, y, color, true);
    }

    public static void draw(Minecraft mc, GuiGraphicsExtractor guiGraphics, String label, int x, int y, int color, boolean shadow) {
        // Using MAX_VALUE as a flag to not render
        if (x != Integer.MAX_VALUE)
        {
            guiGraphics.text(mc.font, label, x, y, color, shadow);
        }
    }
}
