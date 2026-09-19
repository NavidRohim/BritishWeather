package me.brynview.navidrohim.client.gui;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HudCompass
{
    private static final int CENTER_COLOR = ColorHelper.decode("#FF0000").getRGB();
    private static final int COMPASS_BG_COLOR = ColorHelper.rgb(255, 255, 255, 155);
    private static final int COMPASS_BG_SHADOW_COLOUR = ColorHelper.rgb(0, 0, 0, 100);

    private static final String COMPASS_HEADING = "|";
    private static final String ENTITY_LABEL = "⏺";

    static int SCALE_MAX = 255;
    static int DETECTION_DISTANCE = 40;
    static int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10;

    static int sizePercentageOfScaledWidth = 70;

    // value at lowest fov (30): 3.477 blocks value at highest (110): 12.75
    static double EXPAND_INFLATE_WITH_FOV_CONST = 0.118909;

    public static void drawState(GuiGraphicsExtractor guiGraphics, Minecraft mc, int scaledWidth, float _partialTicks) {
        final LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        final float partialTicks = mc.isPaused() ? 0 : _partialTicks;

        final int x = MathHelper.getCompassX(scaledWidth, mc.font.width(COMPASS_HEADING));
        final int compassScaledWidth = (int) (((float) sizePercentageOfScaledWidth / 100f) * (float) scaledWidth);
        final int compassScaledWidthHalf = compassScaledWidth / 2;

        final int y = MathHelper.getCompassY();
        final float yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;

        // Compass background
        drawBackground(guiGraphics, mc, compassScaledWidthHalf, x, y);

        // Render N/E/S/W
        drawCardinal(mc, guiGraphics, yaw, 0, x, y, compassScaledWidth, "S", scaledWidth);
        drawCardinal(mc, guiGraphics, yaw, 90, x, y, compassScaledWidth, "W", scaledWidth);
        drawCardinal(mc, guiGraphics, yaw, 180, x, y, compassScaledWidth, "N", scaledWidth);
        drawCardinal(mc, guiGraphics, yaw, 270, x, y, compassScaledWidth, "E", scaledWidth);

        // Draw all living entities
        drawEntities(mc, player, guiGraphics, partialTicks, yaw, x, y, compassScaledWidthHalf, scaledWidth);

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_HEADING, x, y, CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    private static void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, int compassScaledWidthHalf, int x, int y)
    {
        guiGraphicsExtractor.horizontalLine(x - compassScaledWidthHalf, x + compassScaledWidthHalf, (y + mc.font.lineHeight - 1) / 2, COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine((x - compassScaledWidthHalf) + 1, (x + compassScaledWidthHalf) + 1, ((y + mc.font.lineHeight - 1) / 2) + 1, COMPASS_BG_SHADOW_COLOUR);
    }

    private static void drawEntities(Minecraft mc, LocalPlayer player, GuiGraphicsExtractor guiGraphics, float partialTicks, float yaw, int compassX, int compassY, int compassScaledWidth, int screenScaledWidth)
    {
        // Where player is looking
        Vec3 look = player.getViewVector(partialTicks).scale(DETECTION_DISTANCE);

        // Get bounding box 40m infront of where player is looking
        int fov = mc.options.fov().get();

        AABB normalBB = player.getBoundingBox();
        AABB inFrontOfPlayer = normalBB
                .expandTowards(look.x, 0, look.z)
                .inflate(EXPAND_INFLATE_WITH_FOV_CONST * fov); // inflate so entities don't get cut off from compass

        // For every LivingEntity (includes all living things but not items or arrows) except current player. Does include armour stands
        mc.level.getEntitiesOfClass(LivingEntity.class, inFrontOfPlayer, (en) -> (en != player.asLivingEntity() && !en.getBoundingBox().intersects(normalBB))).stream().limit(MAX_ALLOWED_ENTITIES_ON_COMPASS).forEach(entity ->
                {
                    double angleFromEntity = MathHelper.angleFromPos(entity.position(), player.position());
                    int distanceFromEntity = MathHelper.getDistance(player.position(), entity.position());
                    int iconScale = Math.abs(SCALE_MAX - (SCALE_MAX / DETECTION_DISTANCE * distanceFromEntity));

                    // Entity x offset on screen
                    int ex = MathHelper.getCompassScreenX(yaw, (float) angleFromEntity, compassX, compassScaledWidth, screenScaledWidth);
                    FontHelper.draw(mc, guiGraphics, ENTITY_LABEL, ex, compassY, ColorHelper.rgb(255, 255, 255, iconScale), true, FontHelper.TextType.NONE);
                }
        );
    }

    private static void drawCardinal(Minecraft mc, GuiGraphicsExtractor guiGraphics, float yaw, float angle, int x, int y, int compassScaledWidth, String text, int screenScaledWidth) {
        int dx = MathHelper.getCompassScreenX(yaw, angle, x, compassScaledWidth, screenScaledWidth);
        FontHelper.draw(mc, guiGraphics, text, dx, y, ColorHelper.decode("#FFFFFF").getRGB(), FontHelper.TextType.NONE);
    }
}
