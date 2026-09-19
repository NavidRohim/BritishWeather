package me.brynview.navidrohim.client.gui;

import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class HudCompass
{
    private static final int CENTER_COLOR = ColorHelper.decode("#b02e26").getRGB();

    private static final String COMPASS_CENTER = "|";
    private static final String ENTITY_LABEL = "·";

    static int SCALE_MAX = 255;
    static int DETECTION_DISTANCE = 40;
    static int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10;

    // value at lowest fov (30): 3.477 blocks value at highest (110): 12.75
    static double EXPAND_INFLATE_WITH_FOV_CONST = 0.115909;

    public static void drawState(GuiGraphicsExtractor guiGraphics, Minecraft mc, int scaledWidth, float _partialTicks) {
        final LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        final float partialTicks = mc.isPaused() ? 0 : _partialTicks;

        final int x = MathHelper.getCompassX(scaledWidth, mc.font.width(COMPASS_CENTER));
        final int y = MathHelper.getCompassY();
        final float yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;

        final int bgColor = ColorHelper.rgb(0, 0, 0, 100);

        // Compass background
        guiGraphics.fill(x - 92, y - 1, x + 96, y + mc.font.lineHeight - 1, bgColor);

        // Render N/E/S/W
        drawCardinal(mc, guiGraphics, yaw, 0, x, y, "S");
        drawCardinal(mc, guiGraphics, yaw, 90, x, y, "W");
        drawCardinal(mc, guiGraphics, yaw, 180, x, y, "N");
        drawCardinal(mc, guiGraphics, yaw, 270, x, y, "E");

        // Draw all living entities
        drawEntities(mc, player, guiGraphics, partialTicks, yaw, x, y);

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_CENTER, x, y, CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    private static void drawEntities(Minecraft mc, LocalPlayer player, GuiGraphicsExtractor guiGraphics, float partialTicks, float yaw, int compassX, int compassY)
    {
        // Where player is looking
        Vec3 look = player.getViewVector(partialTicks).scale(DETECTION_DISTANCE);

        // Get bounding box 40m infront of where player is looking
        int fov = mc.options.fov().get();

        AABB normalBB = player.getBoundingBox().inflate(1);
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
                    int ex = MathHelper.getCompassScreenX(yaw, (float) angleFromEntity, compassX, 1);
                    FontHelper.draw(mc, guiGraphics, ENTITY_LABEL, ex, compassY, ColorHelper.rgb(255, 255, 255, iconScale), false, FontHelper.TextType.NONE);
                }
        );
    }

    private static void drawCardinal(Minecraft mc, GuiGraphicsExtractor guiGraphics, float yaw, float angle, int x, int y, String text) {
        int dx = MathHelper.getCompassScreenX(yaw, angle, x, 1.0F);
        FontHelper.draw(mc, guiGraphics, text, dx, y, ColorHelper.decode("#FFFFFF").getRGB(), FontHelper.TextType.NONE);
    }
}
