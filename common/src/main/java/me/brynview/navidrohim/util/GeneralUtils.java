package me.brynview.navidrohim.util;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.hud.compass.Compass;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class GeneralUtils
{
    private static double cMouse = 0;
    private static double cMouseOld = 0;

    public static String getDimensionFromPlayer(@NotNull LocalPlayer player)
    {
        return player.level().dimension().identifier().toString();
    }

    public static void renderFillForElement(GuiGraphicsExtractor renderer, LayoutElement element, int colour)
    {
        renderer.fill(element.getX(), element.getY(), element.getX() + element.getWidth(), element.getY() + element.getHeight(), colour);
    }

    public static void doHouseEventHandleFromMixinToAvoidReloadingEveryDebugSessionForFuckSake(Vector2i wheelXY)
    {
        cMouse = wheelXY.y;
        if (cMouse <= -1.0) // scroll up
        {
            Compass.getInstance().displayZoom(1);
        } else if (cMouse >= 1.0){
            Compass.getInstance().displayZoom(-1);
        }
        cMouseOld = cMouse;
    }
}
