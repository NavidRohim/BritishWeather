package me.brynview.navidrohim.client.hud.compass;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.hud.compass.entry.PersistentEntriesManager;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.client.screen.PinCreationScreen;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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

public class Compass
{
    public Compass()
    {
        mc = Minecraft.getInstance();
        util = new RenderUtils();
    }

    public final class RenderUtils
    {

        public int getTextLocationY()
        {
            return yTextMiddle + mc.font.lineHeight + 3;
        }

        public int shiftColourToOpacity(int colour, int opacity)
        {
            return (colour & 0x00FFFFFF) | (opacity << 24);
        }

        public int getColourForEntity(LivingEntity entity, @Nullable LocalPlayer owner, int opacity)
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
                        return shiftColourToOpacity(ColorHelper.WHITE, opacity);
                    }
                }
                default ->
                {
                }
            }
            return shiftColourToOpacity(ColorHelper.NEUTRAL, opacity);
        }

        public String getActualDegreesFromYaw()
        {
            return (Mth.wrapDegrees(yawInt) + 180) % 360 + "°";
        }

        /*
        Get centered X position for a string, accounting for the width of the string. Only do in render thread
         */
        public int getCenteredXForString(String str, int xPlacement)
        {
            return xPlacement - (mc.font.width(str) / 2);
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

            // If element should stay on right edge of compass
            if (!shouldDisappearWhenOOB && (aDist >= compassScaledWidthHalf))
            {
                return compassX + compassScaledWidthHalf;
            } else if (!shouldDisappearWhenOOB && aDist <= -compassScaledWidthHalf) // Same but left edge
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
    private static Compass INSTANCE;

    // Non-final general variables

    // Set to true right after the entry HUD has been rendered
    // Set to false on the first render pass when the entry HUD disappears.
    // Used to call tick methods like endTick and startTick
    private boolean shouldStartTick = false;


    // Yaw at the current render pass
    private float yaw;
    private int yawInt;

    // Compass width scaled with the compass size config value
    private int compassScaledWidth;
    private int compassScaledWidthHalf;

    // x coordinate of the compass. Can change due to scaling
    public int compassX;
    public int yTextMiddle;

    public Minecraft mc;
    public RenderUtils util;

    public void render(GuiGraphicsExtractor graphics, int scaledWidth, float partialTicks)
    {
        render(graphics, scaledWidth, partialTicks, null, BritishWeather.getConfig().getCompassY());
    }

    public void render(GuiGraphicsExtractor guiGraphics, int scaledWidth, float partialTicks, @Nullable PinCreationScreen dummyEntryProvider, int y)
    {
        final LocalPlayer player = mc.player;

        // Not sure if this can ever be null when this is rendered.
        if ((mc.gui.screen() instanceof PinCreationScreen && dummyEntryProvider == null) || player == null)
        {
            return;
        }
        // uncomment this if something weird happens in pause menu
        //final float partialTicks = mc.isPaused() ? 0 : _partialTicks;
        this.compassX = RenderUtils.getCompassX(scaledWidth, mc.font.width(COMPASS_HEADING));

        final int yMiddle = (y + mc.font.lineHeight - 1) / 2;
        this.yTextMiddle = yMiddle - mc.font.lineHeight / 2;

        this.compassScaledWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) scaledWidth); // All these float are dumb
        this.compassScaledWidthHalf = compassScaledWidth / 2;

        this.yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;
        this.yawInt = (int) yaw;

        // Compass background
        drawBackground(guiGraphics, mc, yMiddle);

        // Check if the entry HUD key is pressed.
        if (!ClientCommon.PROVIDER.isDown() && dummyEntryProvider == null)
        {
            // Will be true on the first render pass after entry HUD disappears.
            if (!shouldStartTick)
            {
                PersistentEntriesManager.ProviderRegistry.stopTickAll(player);
                PersistentEntriesManager.save();
            }

            // Draw all living entities
            drawEntities(player, guiGraphics, partialTicks);

            // Render N/E/S/W
            drawCardinal(guiGraphics, 0,  "S");
            drawCardinal(guiGraphics, 90,  "W");
            drawCardinal(guiGraphics, 180,   "N");
            drawCardinal(guiGraphics, 270,   "E");

            // Render current heading in degrees if config allows
            if (BritishWeather.getConfig().shouldRenderHeading())
            {
                String friendlyDeg = util.getActualDegreesFromYaw(); // Number from 0 to 360
                FontHelper.draw(mc, guiGraphics, friendlyDeg, util.getCenteredXForString(friendlyDeg, this.compassX), util.getTextLocationY(), ColorHelper.CENTER_COLOR, false, FontHelper.TextType.LABEL);
            }

            // Rendering normal compass now, so set to false so stop tick isn't sent multiple times
            shouldStartTick = true;
        } else
        {
            // Draw entry groups and entries.
            drawEntries(guiGraphics, player, dummyEntryProvider);
            shouldStartTick = false;
        }

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_HEADING, this.compassX, this.yTextMiddle, ColorHelper.CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    private void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, int y)
    {
        // End of the compass on far left
        int farLeftX = this.compassX - this.compassScaledWidthHalf;

        // End of the compass on far right
        int farRightX = this.compassX + this.compassScaledWidthHalf;

        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)

        //int brightnessChange = Math.abs(SCALE_MAX - (SCALE_MAX / 10 * rawBrightness));
        //Constants.LOG.info(String.valueOf(brightnessChange));
        guiGraphicsExtractor.horizontalLine(farLeftX + 1, farRightX, y - 1, ColorHelper.COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(farLeftX + 2, farRightX, y, ColorHelper.COMPASS_BG_SHADOW_COLOUR);

        // Draw the two little caps on each end of the compass. Inspired from the God of War 2018 compass.
        FontHelper.draw(mc, guiGraphicsExtractor, ">", farLeftX - 3, y - mc.font.lineHeight / 2, ColorHelper.COMPASS_BG_COLOR, true, FontHelper.TextType.NONE);
        FontHelper.draw(mc, guiGraphicsExtractor, "<", farRightX, y - mc.font.lineHeight / 2, ColorHelper.COMPASS_BG_COLOR, true, FontHelper.TextType.NONE);
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
                    // Angle from us to the entity
                    double angleFromEntity = MathHelper.angleFromPos(entity.position(), player.position());
                    // Distance form entity. Used for the icon opacity. Further away, more transparent. Closer, more opaque
                    int distanceFromEntity = MathHelper.getDistance(player.position(), entity.position());
                    // SCALE_MAX is the maximum value iconScale can be. Cannot be over 255 because 255 is white.
                    int iconScale = Math.abs(SCALE_MAX - (SCALE_MAX / DETECTION_DISTANCE * distanceFromEntity));
                    int livingEntity = util.getColourForEntity(entity, player, iconScale); // Colour entity will be on the compass.

                    // Entity x offset on screen. ex will be MAX_VALUE if the entity is off-screen.
                    int ex = util.getCompassScreenX((float) angleFromEntity);
                    FontHelper.draw(mc, guiGraphics, ENTITY_LABEL, ex, yTextMiddle, livingEntity, false, FontHelper.TextType.NONE);

                }
        );
    }

    /*
    Draw a cardinal direction label at specified angle
     */
    private void drawCardinal(GuiGraphicsExtractor guiGraphics, float angle, String text)
    {
        int dx = util.getCompassScreenX(angle) - 3;
        FontHelper.draw(mc, guiGraphics, text, dx, yTextMiddle, ColorHelper.WHITE, true, FontHelper.TextType.NONE);
    }

    /*
    Draw all entries and entry groups
     */
    private void drawEntries(GuiGraphicsExtractor guiGraphicsExtractor, LocalPlayer player, @Nullable PinCreationScreen dummyProvider)
    {
        if (dummyProvider != null)
        {
            DefaultEntry.drawRawEntry(
                    guiGraphicsExtractor,
                    mc,
                    player,
                    player.position().add(5, 0, 5),
                    dummyProvider.getMarker(),
                    dummyProvider.getColour(),
                    1,
                    true,
                    "DEMO",
                    50
            );
            return;
        }

        // Draw normal entries.
        for (DefaultEntry entry : PersistentEntriesManager.ProviderRegistry.getEntries())
        {
            entry.draw(guiGraphicsExtractor, player, shouldStartTick);
        }

        // Draw entries inside of entry groups.
        for (DefaultEntryGroup group : PersistentEntriesManager.ProviderRegistry.PROVIDER_GROUPS)
        {
            if (shouldStartTick)
            {
                group.startTick(player);
            }

            group.getEntries().forEach(groupEntry -> {
                groupEntry.draw(guiGraphicsExtractor, player, shouldStartTick);
            });
        }
    }

    // Static methods

    public static Compass init()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new Compass();
            return INSTANCE;
        }

        // HudCompass is a singleton
        throw new RuntimeException("HudCompass already initialized.");
    }

    public static Compass getInstance()
    {
        return INSTANCE;
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
