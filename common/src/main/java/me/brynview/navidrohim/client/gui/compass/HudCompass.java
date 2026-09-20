package me.brynview.navidrohim.client.gui.compass;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.providers.CompassProvider;
import me.brynview.navidrohim.client.gui.compass.providers.impl.HoldingWhateverProvider;
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

import java.util.ArrayList;
import java.util.List;

public class HudCompass
{

    private static final List<CompassProvider> PROVIDERS = List.of(new HoldingWhateverProvider());

    private static final int CENTER_COLOR = ColorHelper.decode("#FF0000").getRGB(); // Red
    private static final int COMPASS_BG_COLOR = ColorHelper.rgb(255, 255, 255, 190);
    private static final int COMPASS_BG_SHADOW_COLOUR = ColorHelper.rgb(0, 0, 0, 100);

    private static final String COMPASS_HEADING = "|";
    private static final String ENTITY_LABEL = "⏺";

    private static double EXPAND_INFLATE_WITH_FOV_CONST = 0.119; // value at lowest fov (30): 3.477 blocks value at highest (110): 13.07

    private static int SCALE_MAX = 255; // Max value of what the entity distance scale should be (entity further away = lower, closer = higher)
    private static int DETECTION_DISTANCE = 40; // How far to check in front of the player for entities
    private static int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10; // Max entities allowed on compass

    public static void drawState(GuiGraphicsExtractor guiGraphics, Minecraft mc, int scaledWidth, float partialTicks) {
        final LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        // uncomment this if something weird happens in pause menu
        //final float partialTicks = mc.isPaused() ? 0 : _partialTicks;
        final int x = MathHelper.getCompassX(scaledWidth, mc.font.width(COMPASS_HEADING));

        final int yMiddle = (BritishWeather.getConfig().getCompassY() + mc.font.lineHeight - 1) / 2;
        final int yMiddleForText = yMiddle - mc.font.lineHeight / 2;

        final int compassScaledWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) scaledWidth); // All these float are dumb
        final int compassScaledWidthHalf = compassScaledWidth / 2;

        final float yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;

        // Compass background
        drawBackground(guiGraphics, mc, compassScaledWidthHalf, x, yMiddle);

        // Render N/E/S/W
        drawCardinal(mc, guiGraphics, yaw, 0, x, yMiddleForText, compassScaledWidth, "S");
        drawCardinal(mc, guiGraphics, yaw, 90, x, yMiddleForText, compassScaledWidth, "W");
        drawCardinal(mc, guiGraphics, yaw, 180, x, yMiddleForText, compassScaledWidth, "N");
        drawCardinal(mc, guiGraphics, yaw, 270, x, yMiddleForText, compassScaledWidth, "E");

        // Draw all living entities
        drawEntities(mc, player, guiGraphics, partialTicks, yaw, x, yMiddleForText, compassScaledWidthHalf);
        drawProviders(guiGraphics, mc, player, (int) yaw, compassScaledWidth, x, yMiddleForText, partialTicks);

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_HEADING, x, yMiddleForText, CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    private static void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, int compassScaledWidthHalf, int x, int y)
    {
        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)
        int farLeftX = x - compassScaledWidthHalf;
        int farRightX = x + compassScaledWidthHalf;
        int aY = y - 1;

        guiGraphicsExtractor.horizontalLine(farLeftX + 1 , farRightX, aY, COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(farLeftX + 2, farRightX, aY + 1, COMPASS_BG_SHADOW_COLOUR);

        FontHelper.draw(mc, guiGraphicsExtractor, "<", farLeftX, 1 + aY - mc.font.lineHeight / 2, COMPASS_BG_COLOR, false, FontHelper.TextType.NONE);
        FontHelper.draw(mc, guiGraphicsExtractor, ">", farRightX - 2, 1 + aY - mc.font.lineHeight / 2, COMPASS_BG_COLOR, false, FontHelper.TextType.NONE);
    }

    private static void drawEntities(Minecraft mc, LocalPlayer player, GuiGraphicsExtractor guiGraphics, float partialTicks, float yaw, int compassX, int y, int compassScaledWidth)
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
                    int ex = MathHelper.getCompassScreenX(yaw, (float) angleFromEntity, compassX, compassScaledWidth);
                    FontHelper.draw(mc, guiGraphics, ENTITY_LABEL, ex, y, ColorHelper.rgb(255, 255, 255, iconScale), true, FontHelper.TextType.NONE);
                }
        );
    }

    private static void drawCardinal(Minecraft mc, GuiGraphicsExtractor guiGraphics, float yaw, float angle, int x, int y, int compassScaledWidth, String text) {
        int dx = MathHelper.getCompassScreenX(yaw, angle, x, compassScaledWidth);
        FontHelper.draw(mc, guiGraphics, text, dx, y, ColorHelper.decode("#FFFFFF").getRGB(), FontHelper.TextType.NONE);
    }

    private static void drawProviders(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, LocalPlayer player, int yaw, int compassScaledWidth, int compassWindowX, int compassWindowY, float partialTick)
    {
        for (CompassProvider provider : PROVIDERS)
        {
            if (partialTick % 20 == 0)
            {
                provider.tick(player);
            }

            if (provider.shouldShow())
            {
                Vec3 providerPosition = provider.getPosition(player);
                double angleFromPosition = MathHelper.angleFromPos(providerPosition, player.position());
                int compassX = MathHelper.getCompassScreenX(yaw, (float) angleFromPosition, compassWindowX, compassScaledWidth, false);

                FontHelper.draw(mc, guiGraphicsExtractor, provider.getText(), compassX, compassWindowY, CENTER_COLOR, FontHelper.TextType.NONE);
            }
        }
    }
}
