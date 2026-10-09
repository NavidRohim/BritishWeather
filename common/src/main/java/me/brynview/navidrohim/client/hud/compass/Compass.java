package me.brynview.navidrohim.client.hud.compass;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.ClientKeybinds;
import me.brynview.navidrohim.client.hud.compass.entry.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.Pin;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.EntryRenderable;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.client.screen.PinCreationScreen;
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
        private static final float BASE_VISIBLE_AMOUNT = 180F;
        public String getActualDegreesFromYaw()
        {
            return (Mth.wrapDegrees(yawInt) + 180) % 360 + "°";
        }

        // X placements

        /*
        Get centered X position for a string, accounting for the width of the string. Only do in render thread
         */
        public int getCenteredXForString(String str, int xPlacement)
        {
            return xPlacement - (mc.font.width(str) / 2);
        }

        public boolean isXOutOfBounds(int x)
        {
            return x <= compassX - compassScaledWidthHalf || x >= compassX + compassScaledWidthHalf;
        }

        public boolean isXHighlightable(int x)
        {
            int highlightBounds = getHighlightBounds();
            return x <= compassX + highlightBounds && x >= compassX - highlightBounds;
        }

        public int getHighlightBounds()
        {
            return Math.min(HIGHLIGHT_BOUNDS * zoomAmount, compassScaledWidthHalf);
        }

        public static int getCompassX(int screenWidth, int textWidth)
        {
            return (screenWidth - textWidth) / 2;
        }

        public int getCompassScreenX(float angle)
        {
            return (int) getCompassScreenX(angle, true);
        }

        public float getCompassScreenX(float angle, boolean shouldDisappearWhenOOB)
        {
            // Text is centered at the center of the screen. aDist is used as an offset
            // Return an int between -180 and +180 (360)
            // yaw: float between 0.0 and 360.0
            // angle: float between 0 and 270
            // aDist example: (yaw = 90, angle = 0) aDist = angle - yaw = -90
            float lerpZoom = Mth.lerp(1, zoomAmountSmoothOld, zoomAmountSmooth);
            float amountOnCompass = BASE_VISIBLE_AMOUNT / lerpZoom;
            float aDist = Mth.wrapDegrees(angle - yaw) * (compassScaledWidth / amountOnCompass);
            float csx = compassX + aDist;

            // Make sure text is not rendered past the compass bounds
            if (Math.abs(aDist) < compassScaledWidthHalf)
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

        // Y funcs

        public int getYRowOnCompass(int row)
        {
            return yTextMiddle + (mc.font.lineHeight * row);
        }

        public int getXForEntry(DefaultEntry entry)
        {
            boolean shouldDisappear = entry instanceof Pin && ((Pin) entry).shouldDisappearWhenNotInView();
            double angleFromPosition = MathHelper.angleFromPos(entry.getPosition(), mc.player.position());
            return (int) getCompassScreenX((float) angleFromPosition, shouldDisappear);
        }
    }

    // Text used for compass decorations
    private static final String COMPASS_HEADING = "|";
    private static final String ENTITY_LABEL = "⏺";

    // Math constants used for rendering
    private static final double EXPAND_INFLATE_WITH_FOV_CONST = 0.119; // value at lowest fov (30): 3.477 blocks value at highest (110): 13.07
    private static final int DETECTION_DISTANCE = 40; // How far to check in front of the player for entities
    private static final int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10; // Max entities allowed on compass
    private static final int ZOOM_ALERT_DURATION = 40;

    public static final int HIGHLIGHT_BOUNDS = 7;

    private static Compass INSTANCE;

    // Non-final general variables

    // Set to true right after the entry HUD has been rendered
    // Set to false on the first render pass when the entry HUD disappears.
    // Used to call tick methods like endTick and startTick
    private boolean shouldStartTick = false;
    private boolean isHoldingRemovalKey = false;
    // Yaw at the current render pass
    private float yaw;
    private int yawInt;

    private int zoomAlertTick = 0;
    private int zoomAmount = 1;
    private float zoomAmountSmooth = 1;
    private float zoomAmountSmoothOld = 1;

    // Compass width scaled with the compass size config value
    private int compassScaledWidth;
    public int compassScaledWidthHalf;

    // x coordinate of the compass. Can change due to scaling
    public int compassX;
    private int farLeftX;
    private int farRightX;

    public int yTextMiddle;
    public int yMiddle;
    public float partialTicks;

    public Minecraft mc;
    public RenderUtils util;
    private @Nullable DefaultEntry highlightedEntry;


    private final List<DefaultEntry> entriesInSnapArea = new ArrayList<>();
    private int snapIndex = 0;

    private void calculateDimensions(int scaledWidth, int y, float partialTicks, @NotNull LocalPlayer player)
    {

        // uncomment this if something weird happens in pause menu
        //final float partialTicks = mc.isPaused() ? 0 : _partialTicks;
        this.partialTicks = partialTicks;
        this.compassX = RenderUtils.getCompassX(scaledWidth, mc.font.width(COMPASS_HEADING));

        this.yMiddle = (y + mc.font.lineHeight - 1) / 2;
        this.yTextMiddle = yMiddle - mc.font.lineHeight / 2;

        this.compassScaledWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) scaledWidth); // All these float are dumb
        this.compassScaledWidthHalf = compassScaledWidth / 2;

        this.farLeftX = compassX - compassScaledWidthHalf;
        this.farRightX = compassX + compassScaledWidthHalf;

        this.yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;
        this.yawInt = (int) yaw;
    }

    public void render(GuiGraphicsExtractor graphics, int scaledWidth, float partialTicks)
    {
        render(graphics, scaledWidth, partialTicks, null, BritishWeather.getConfig().getCompassY());
    }

    public void render(GuiGraphicsExtractor guiGraphics, int scaledWidth, float partialTicks, @Nullable EntryRenderable dummyEntryProvider, int y)
    {
        final LocalPlayer player = mc.player;

        // Not sure if this can ever be null when this is rendered.
        if ((mc.gui.screen() instanceof PinCreationScreen && dummyEntryProvider == null) || player == null)
        {
            return;
        }

        // Calculate compass dimensions
        calculateDimensions(scaledWidth, y, partialTicks, player);

        // Compass background
        drawBackground(guiGraphics, mc, yMiddle);

        // Check if the entry HUD key is pressed.
        if (!ClientKeybinds.ENTRY_HUD_KEY.isDown() && dummyEntryProvider == null)
        {
            // Will be true on the first render pass after entry HUD disappears.
            if (!shouldStartTick)
            {
                EntryManager.ProviderRegistry.stopTickAll(player);
            }

            // Draw all living entities
            if (BritishWeather.getConfig().shouldShowEntitiesOnCompass())
            {
                snapIndex = 0;
                drawEntities(player, guiGraphics, partialTicks);
            }

            // Render N/E/S/W
            drawCardinal(guiGraphics, 0,  "S");
            drawCardinal(guiGraphics, 90,  "W");
            drawCardinal(guiGraphics, 180,   "N");
            drawCardinal(guiGraphics, 270,   "E");

            // Render current heading in degrees if config allows
            if (BritishWeather.getConfig().shouldRenderHeading())
            {
                String friendlyDeg = util.getActualDegreesFromYaw(); // Number from 0 to 360
                FontHelper.draw(mc, guiGraphics, friendlyDeg, util.getCenteredXForString(friendlyDeg, this.compassX), util.getYRowOnCompass(1), ColorHelper.CENTER_COLOR, false, FontHelper.TextType.LABEL);
            }

            // Rendering normal compass now, so set to false so stop tick isn't sent multiple times
            shouldStartTick = true;
        } else
        {
            // Draw entry groups and entries.
            drawEntries(guiGraphics, player, dummyEntryProvider);
            drawZoomLevelEffects(guiGraphics, ColorHelper.WHITE);
            shouldStartTick = false;
        }

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_HEADING, this.compassX, this.yTextMiddle, ColorHelper.CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    private void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, int y)
    {
        // End of the compass on far left

        // End of the compass on far right

        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)
        int decoY = y - mc.font.lineHeight / 2;

        guiGraphicsExtractor.horizontalLine(farLeftX + 1, farRightX, y - 1, ColorHelper.COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(farLeftX + 2, farRightX, y, ColorHelper.COMPASS_BG_SHADOW_COLOUR);

        if (zoomAlertTick > 0)
        {
            int opacity = ColorHelper.getOpacity(ZOOM_ALERT_DURATION, zoomAlertTick, false);
            int colourShiftOpacityWhite = ColorHelper.shiftColourToOpacity(ColorHelper.WHITE, opacity);

            drawZoomLevelEffects(guiGraphicsExtractor, colourShiftOpacityWhite);
        }

        // Draw the two little caps on each end of the compass. Inspired from the God of War 2018 compass.
        FontHelper.draw(mc, guiGraphicsExtractor, ">", farLeftX - 3, decoY, ColorHelper.COMPASS_BG_COLOR, true, FontHelper.TextType.NONE);
        FontHelper.draw(mc, guiGraphicsExtractor, "<", farRightX, decoY, ColorHelper.COMPASS_BG_COLOR, true, FontHelper.TextType.NONE);
    }

    private void drawZoomLevelEffects(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        String amount = String.valueOf(zoomAmount);
        FontHelper.draw(mc, guiGraphicsExtractor, amount, farLeftX - (mc.font.width(amount) + 5), yTextMiddle, colour, true, FontHelper.TextType.VALUE);

        if (zoomAmount > 1)
        {
            int bounds = util.getHighlightBounds();
            FontHelper.draw(mc, guiGraphicsExtractor, "+", compassX - bounds, yTextMiddle, ColorHelper.COMPASS_BG_COLOR, true, FontHelper.TextType.VALUE);
            FontHelper.draw(mc, guiGraphicsExtractor, "+", compassX + bounds, yTextMiddle, ColorHelper.COMPASS_BG_COLOR, true, FontHelper.TextType.VALUE);
        }
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
                    int iconScale = ColorHelper.getOpacity(DETECTION_DISTANCE, distanceFromEntity, true);
                    int livingEntity = ColorHelper.getColourForEntity(entity, player, iconScale); // Colour entity will be on the compass.

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
    private void drawEntries(GuiGraphicsExtractor guiGraphicsExtractor, LocalPlayer player, @Nullable EntryRenderable dummyProvider)
    {
        if (dummyProvider != null)
        {
            DefaultEntry.drawRawEntry(
                    guiGraphicsExtractor,
                    mc,
                    player,
                    this,
                    dummyProvider.shouldShowDistance(),
                    null,
                    true,
                    dummyProvider,
                    compassX + 50,
                    dummyProvider.getColour()
            );
            return;
        }

        this.entriesInSnapArea.clear();

        boolean didHighlight = false;
        int highlightedElementX = Integer.MAX_VALUE;
        int highlightedElementColour = ColorHelper.WHITE;

        // Draw normal entries.
        for (DefaultEntry entry : EntryManager.ProviderRegistry.getEntries())
        {
            int compassXForEntry = util.getXForEntry(entry);

            if (util.isXHighlightable(compassXForEntry))
            {
                boolean didHighlightEntry = setInSnapArea(entry, compassX == compassXForEntry);
                if (didHighlightEntry && !didHighlight) {
                    highlightedElementX = compassXForEntry;
                    highlightedElementColour = entry.getColour();
                    didHighlight = true;

                    entry.setFocused(true);
                } else {
                    entry.draw(guiGraphicsExtractor, player, shouldStartTick, compassXForEntry, entry.getHighlightColour());
                    entry.setFocused(false);
                }
            } else {
                entry.draw(guiGraphicsExtractor, player, shouldStartTick, compassXForEntry, entry.getHighlightColour());
                entry.setFocused(false);
            }
        }

        if (!didHighlight)
        {
            if (highlightedEntry != null)
            {
                highlightedEntry.setFocused(false);
            }
            highlightedEntry = null;
        } else {
            highlightedEntry.draw(guiGraphicsExtractor, player, shouldStartTick, highlightedElementX, highlightedElementColour);
        }

        // Draw entries inside of entry groups.
        for (DefaultEntryGroup group : EntryManager.ProviderRegistry.PROVIDER_GROUPS)
        {
            if (group.isBuiltin() && !BritishWeather.getConfig().shouldShowBuiltinObjectives())
            {
                continue;
            }

            if (shouldStartTick)
            {
                group.startTick(player);
            }

            group.getEntries().forEach(groupEntry -> {
                int xForEntry = util.getXForEntry(groupEntry);
                groupEntry.draw(guiGraphicsExtractor, player, shouldStartTick, xForEntry, groupEntry.getColour());
            });
        }
    }


    public boolean setInSnapArea(DefaultEntry entry, boolean absolute)
    {
        this.entriesInSnapArea.add(entry);
        if (highlightedEntry == null || highlightedEntry == entry || absolute)
        {
            this.highlightedEntry = entry;
            return true;
        }
        return false;
    }

    public void displayZoom(int zoomIncrease)
    {
        int newZoomAmount = zoomAmount + zoomIncrease;

        if (newZoomAmount < 1)
        {
            zoomAmount = 1;
            return;
        }

        zoomAmount = newZoomAmount;
        zoomAmountSmoothOld = zoomAmountSmooth;
        zoomAmountSmooth = 1.0f + (zoomAmount - 1.0f) * 0.5f;
        zoomAlertTick = Compass.ZOOM_ALERT_DURATION;

        mc.player.playSound(ClientCommon.SCROLL, 0.01f, 1.5f);
    }

    private void tick()
    {
        zoomAlertTick--;

        if (highlightedEntry != null)
        {
            if (ClientKeybinds.ENTRY_HUD_KEY.isDown() && !isHoldingRemovalKey)
            {
                if (ClientKeybinds.DELETION.isDown())
                {
                    EntryManager.ProviderRegistry.removeEntry(this.highlightedEntry);
                    this.highlightedEntry = null;
                }

                if (ClientKeybinds.DEBUG_ON_PRESS.isDown())
                {
                    Vec3 pos = this.highlightedEntry.getPosition();
                    mc.player.connection.sendCommand("tp @s %s %s %s".formatted(pos.x, pos.y, pos.z));
                }

                if (ClientKeybinds.LEFT_SNAP.isDown())
                {
                    snap(1);
                }

                if (ClientKeybinds.RIGHT_SNAP.isDown())
                {
                    snap(-1);
                }
            } else {
                this.isHoldingRemovalKey = false;
            }
        }
    }

    private void snap(int index)
    {
        // Check bounds
        if (entriesInSnapArea.isEmpty())
        {
            return;
        }

        if ( (snapIndex == 0 && index < 0) || (snapIndex + index >= this.entriesInSnapArea.size()) )
        {
            snapIndex = 0;
            index = 0;
        }

        snapIndex += index;
        DefaultEntry snappedCurrentEntry = entriesInSnapArea.get(snapIndex);

        double a = MathHelper.angleFromPos(snappedCurrentEntry.getPosition(), mc.player.position());
        mc.player.setYRot((float) a);
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
        EntryManager.ProviderRegistry.addEntry(provider);
    }

    public static Collection<DefaultEntry> getEntries()
    {
        return EntryManager.ProviderRegistry.getEntries();
    }

    public static void tick(@NotNull LocalPlayer player)
    {
        Compass compass = getInstance();

        EntryManager.ProviderRegistry.tick(player);
        compass.tick();
    }
}
