package me.brynview.navidrohim.client.gui.compass;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.gui.compass.providers.PersistentEntriesManager;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultTimedEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HudCompass
{

    private static HudCompass INSTANCE;

    public static HudCompass init()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new HudCompass();
            return INSTANCE;
        }
        throw new RuntimeException("HudCompass already initialized.");
    }

    public static HudCompass getInstance()
    {
        return INSTANCE;
    }

    public HudCompass()
    {
        mc = Minecraft.getInstance();
        util = new RenderUtils();
    }

    private final class RenderUtils
    {

        private int getTextLocationY()
        {
            return yTextMiddle + mc.font.lineHeight + 3;
        }

        private int shiftColourToOpacity(int colour, int opacity)
        {
            return (colour & 0x00FFFFFF) | (opacity << 24);
        }

        private int getColourForEntity(LivingEntity entity, @Nullable LocalPlayer owner, int opacity)
        {
            switch (entity)
            {
                case Player _ ->
                {
                    return shiftColourToOpacity(ColorHelper.PLAYER, opacity);
                }
                case Monster _ ->
                {
                    return shiftColourToOpacity(ColorHelper.HOSTILE, opacity);
                }
                case TamableAnimal tamableAnimal ->
                {
                    if (tamableAnimal.getOwnerReference() != null && tamableAnimal.getOwnerReference().getUUID().equals(owner.getUUID()))
                    {
                        return shiftColourToOpacity(ColorHelper.ALLY, opacity);
                    }
                }
                default ->
                {
                }
            }
            return shiftColourToOpacity(ColorHelper.NEUTRAL, opacity);
        }

        private String getActualDegreesFromYaw()
        {
            return (Mth.wrapDegrees(yawInt) + 180) % 360 + "°";
        }

        private int getXForString(String str)
        {
            return compassX - (mc.font.width(str) / 2);
        }

        public int getCompassScreenX(float angle, boolean shouldDisappearWhenOOB)
        {
            // Text is centered at the center of the screen. aDist is used as an offset
            // Return an int between -180 and +180 (360)
            // yaw: float between 0.0 and 360.0
            // angle: float between 0 and 270
            // aDist example: (yaw = 90, angle = 0) aDist = angle - yaw = -90

            int compassScaledWidthHalf = compassScaledWidth / 2;
            int aDist = (int) (Mth.wrapDegrees(angle - yaw) * ((float) compassScaledWidth / 180));
            int absADist = Math.abs(aDist);
            int csx = compassX + aDist;

            // Make sure text is not rendered past the compass bounds

            if (absADist < compassScaledWidthHalf)
            {
                return csx; // x will be center of the screen. aDist is the offset where to render text
            }

            if (!shouldDisappearWhenOOB && (aDist >= compassScaledWidthHalf))
            {
                return compassX + compassScaledWidthHalf;
            } else if (!shouldDisappearWhenOOB && aDist <= -compassScaledWidthHalf)
            {
                return compassX - compassScaledWidthHalf;
            }

            // Return max int to make sure whatever is going out of bounds fucks right off so we don't see it
            return Integer.MAX_VALUE;
        }

        public int getCompassScreenX(float angle)
        {
            return getCompassScreenX(angle, true);
        }

        public boolean isXOutOfBounds(int x)
        {
            return x <= compassX - compassScaledWidthHalf || x >= compassX + compassScaledWidthHalf;
        }

        public static int getCompassX(int screenWidth, int textWidth)
        {
            return (screenWidth - textWidth) / 2;
        }
    }

    // Text used for compass decorations
    private static final String COMPASS_HEADING = "|";
    private static final String ENTITY_LABEL = "⏺";

    // Math constants used for rendering
    private static final double EXPAND_INFLATE_WITH_FOV_CONST = 0.119; // value at lowest fov (30): 3.477 blocks value at highest (110): 13.07
    private static final int SCALE_MAX = 255; // Max value of what the entity distance scale should be (entity further away = lower, closer = higher)
    private static final int DETECTION_DISTANCE = 40; // How far to check in front of the player for entities
    private static final int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10; // Max entities allowed on compass

    // Non-final general variables
    private boolean didRenderProviders = false;

    private float yaw;
    private int yawInt;
    private int compassScaledWidth;
    private int compassScaledWidthHalf;

    private int compassX;
    private int yTextMiddle;

    private Minecraft mc;
    private RenderUtils util;

    public void render(GuiGraphicsExtractor guiGraphics, Minecraft mc, int scaledWidth, float partialTicks)
    {
        final LocalPlayer player = mc.player;

        if (player == null)
        {
            return;
        }

        // uncomment this if something weird happens in pause menu
        //final float partialTicks = mc.isPaused() ? 0 : _partialTicks;
        this.compassX = RenderUtils.getCompassX(scaledWidth, mc.font.width(COMPASS_HEADING));

        final int yMiddle = (BritishWeather.getConfig().getCompassY() + mc.font.lineHeight - 1) / 2;
        this.yTextMiddle = yMiddle - mc.font.lineHeight / 2;

        this.compassScaledWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) scaledWidth); // All these float are dumb
        this.compassScaledWidthHalf = compassScaledWidth / 2;

        this.yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;
        this.yawInt = (int) yaw;

        // Compass background
        drawBackground(guiGraphics, mc, yMiddle);

        if (!ClientCommon.PROVIDER.isDown())
        {
            if (didRenderProviders)
            {
                PersistentEntriesManager.ProviderRegistry.stopTickAll(player);
                PersistentEntriesManager.save();
            }

            // Render N/E/S/W
            drawCardinal(guiGraphics, 0,  "S");
            drawCardinal(guiGraphics, 90,  "W");
            drawCardinal(guiGraphics, 180,   "N");
            drawCardinal(guiGraphics, 270,   "E");

            // Draw all living entities
            drawEntities(player, guiGraphics, partialTicks);

            if (BritishWeather.getConfig().shouldRenderHeading())
            {
                String friendlyDeg = util.getActualDegreesFromYaw();
                FontHelper.draw(mc, guiGraphics, friendlyDeg, util.getXForString(friendlyDeg), util.getTextLocationY(), ColorHelper.CENTER_COLOR, true, FontHelper.TextType.LABEL);
            }

            didRenderProviders = false;
        } else
        {
            drawEntries(guiGraphics, player);
            didRenderProviders = true;
        }

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_HEADING, this.compassX, this.yTextMiddle, ColorHelper.CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    private void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, int y)
    {
        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)
        int farLeftX = this.compassX - this.compassScaledWidthHalf;
        int farRightX = this.compassX + this.compassScaledWidthHalf;
        int aY = y - 1;

        guiGraphicsExtractor.horizontalLine(farLeftX + 1, farRightX, aY, ColorHelper.COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(farLeftX + 2, farRightX, aY + 1, ColorHelper.COMPASS_BG_SHADOW_COLOUR);

        FontHelper.draw(mc, guiGraphicsExtractor, "<", farLeftX, 1 + aY - mc.font.lineHeight / 2, ColorHelper.COMPASS_BG_COLOR, false, FontHelper.TextType.NONE);
        FontHelper.draw(mc, guiGraphicsExtractor, ">", farRightX - 2, 1 + aY - mc.font.lineHeight / 2, ColorHelper.COMPASS_BG_COLOR, false, FontHelper.TextType.NONE);
    }

    private void drawEntities(LocalPlayer player, GuiGraphicsExtractor guiGraphics, float partialTicks)
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
                    int livingEntity = util.getColourForEntity(entity, player, iconScale);

                    // Entity x offset on screen
                    int ex = util.getCompassScreenX((float) angleFromEntity);
                    if (ex != Integer.MAX_VALUE)
                    {
                        FontHelper.draw(mc, guiGraphics, ENTITY_LABEL, ex, yTextMiddle, livingEntity, true, FontHelper.TextType.NONE);
                    }
                }
        );
    }

    private void drawCardinal(GuiGraphicsExtractor guiGraphics, float angle, String text)
    {
        int dx = util.getCompassScreenX(angle) - 3;
        if (dx != Integer.MAX_VALUE)
        {
            FontHelper.draw(mc, guiGraphics, text, dx, yTextMiddle, ColorHelper.WHITE, FontHelper.TextType.NONE);
        }
    }

    private void drawEntry(
            GuiGraphicsExtractor guiGraphicsExtractor,
            DefaultEntry entry,
            LocalPlayer player
    )
    {
        if (!entry.shouldRender() || !entry.getLevel().equals(player.level().dimension().identifier().toString()))
        {
            return;
        }

        if (!didRenderProviders)
        {
            entry.startTick(player);
        }

        Vec3 playerPos = player.position();
        Vec3 entryPos = entry.getPosition();

        double angleFromPosition = MathHelper.angleFromPos(entryPos, playerPos);
        int compassX = util.getCompassScreenX((float) angleFromPosition, false);

        FontHelper.draw(mc, guiGraphicsExtractor, entry.getMarker(), compassX - (entry.getMarkerWidthHalf()), this.yTextMiddle, entry.getColour(), FontHelper.TextType.NONE);

        if (entry.shouldShowDistance() && !util.isXOutOfBounds(compassX))
        {
            int distance = MathHelper.getDistance(entryPos, playerPos);

            String suffix = "m ";
            double heightDiff = playerPos.y - entryPos.y;
            if (distance <= 200 && heightDiff >= 3)
            {
                suffix = "↓";
            } else if (distance <= 200 && heightDiff <= -3)
            {
                suffix = "↑";
            }

            if (entry instanceof DefaultTimedEntry && BritishWeather.getConfig().debug())
            {
                String label = ((DefaultTimedEntry) entry).timeMethod.timeLeft() + "s";
                FontHelper.draw(mc, guiGraphicsExtractor, label, util.getXForString(label), this.yTextMiddle - 10, entry.getColour(), FontHelper.TextType.NONE);
            }

            String distanceFromObjective = MathHelper.getDistance(playerPos, entryPos) + suffix;
            FontHelper.draw(mc, guiGraphicsExtractor, distanceFromObjective, util.getXForString(distanceFromObjective) + 2, util.getTextLocationY(), entry.getColour(), FontHelper.TextType.LABEL);

        }
    }

    private void drawEntries(
            GuiGraphicsExtractor guiGraphicsExtractor,
            LocalPlayer player
    )
    {
        for (DefaultEntry entry : PersistentEntriesManager.ProviderRegistry.getEntries())
        {
            drawEntry(guiGraphicsExtractor, entry, player);
        }

        for (DefaultEntryGroup group : PersistentEntriesManager.ProviderRegistry.PROVIDER_GROUPS)
        {
            if (!didRenderProviders)
            {
                group.startTick(player);
            }

            group.getEntries().forEach(groupEntry -> {
                drawEntry(guiGraphicsExtractor, groupEntry, player);
            });
        }
    }

    public static void addEntry(DefaultEntry provider)
    {
        PersistentEntriesManager.ProviderRegistry.addEntry(provider);
    }

    public static Collection<DefaultEntry> getEntries()
    {
        return PersistentEntriesManager.ProviderRegistry.getEntries();
    }

    public static void tick(@NotNull LocalPlayer player)
    {
        PersistentEntriesManager.ProviderRegistry.tick(player);
    }
}
