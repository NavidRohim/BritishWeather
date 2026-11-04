package me.brynview.navidrohim.client.hud.compass;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.ClientKeybinds;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.client.screen.PinManageScreen;
import me.brynview.navidrohim.client.screen.SearchPin;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
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
        public int centerStringAroundX(String str, int xPlacement)
        {
            return centerWidthAroundX(mc.font.width(str), xPlacement);
        }

        public int centerWidthAroundX(int width, int x)
        {
            return x - width / 2;
        }

        public boolean isXOutOfBounds(int x)
        {
            return x == Integer.MAX_VALUE || x < centerX - compassScaledWidthHalf || x > centerX + compassScaledWidthHalf;
        }

        public boolean isXHighlightable(int x)
        {
            return x <= centerX + highlightArea && x >= centerX - highlightArea;
        }

        public int getAngleX(float angle)
        {
            return getAngleX(angle, true);
        }

        public int getAngleX(double angle, boolean shouldDisappearWhenOOB)
        {
            // Text is centered at the center of the screen. aDist is used as an offset
            // Return an int between -180 and +180 (360)
            // yaw: float between 0.0 and 360.0
            // angle: float between 0 and 270
            // aDist example: (yaw = 90, angle = 0) aDist = angle - yaw = -90
            float amountOnCompass = BASE_VISIBLE_AMOUNT / zoomAmountSmooth;
            double aDist = Mth.wrapDegrees(angle - yaw) * (compassScaledWidth / amountOnCompass);
            double csx = centerX + aDist;

            // Make sure text is not rendered past the compass bounds
            if (Math.abs(aDist) < compassScaledWidthHalf)
            {
                return Math.toIntExact(Math.round(csx)); // x will be center of the screen. aDist is the offset where to render text
            }

            // If element should stay on right edge of compass
            if (!shouldDisappearWhenOOB && (aDist >= compassScaledWidthHalf))
            {
                return centerX + compassScaledWidthHalf;
            } else if (!shouldDisappearWhenOOB && aDist <= -compassScaledWidthHalf) // Same but left edge
            {
                return centerX - compassScaledWidthHalf;
            }

            // Return max int to make sure whatever is going out of bounds fucks right off so we don't see it
            return Integer.MAX_VALUE;
        }

        // Y funcs

        public int getYRowOnCompass(double row)
        {
            return (int) (y + (mc.font.lineHeight * row));
        }

        public int getYOnRowCentered(int row, int height)
        {
            return getYRowOnCompass(row) - (height / 4);
        }

        public int getXFromPosition(Vec3 position, boolean disappearWhenOutOfBounds)
        {
            double angleRelativeToPosition = MathHelper.angleFromPos(position, mc.player.position());
            return getAngleX(angleRelativeToPosition, disappearWhenOutOfBounds);
        }
    }

    // Text used for compass decorations
    private static final int ZOOM_ALERT_DURATION = 40;
    private static final int HIGHLIGHT_BOUNDS = 7;

    private static Compass INSTANCE;

    // Non-final general variables

    // Set to true right after the entry HUD has been rendered
    // Set to false on the first render pass when the entry HUD disappears.
    // Used to call tick methods like endTick and startTick
    public float partialTicks;
    private boolean shouldStartTick = false;
    // Yaw at the current render pass
    private float yaw;
    public int yawInt;

    // Compass width scaled with the compass size config value
    private int compassScaledWidth;
    public int compassScaledWidthHalf;

    // x coordinate of the compass. Can change due to scaling
    public int centerX;

    public int leftX;
    public int leftContentX;

    public int rightX;
    public int rightContentX;

    public int y;
    public int contentY;

    public Minecraft mc;
    public RenderUtils util;
    private KeyMapping currentHeldKey = null;
    public int entriesOnLeftEdge = 0;
    public int entriesOnRightEdge = 0;

    private final TreeMap<Float, AbstractEntry> renderMap = new TreeMap<>(Comparator.reverseOrder());

    private @Nullable PinEntry trackedPin;
    private @Nullable AbstractEntry highlightedEntry;

    private int zoomAlertTick = 0;
    private int zoomAmount = 1;
    private float zoomAmountSmooth = 1;

    private int highlightArea = 0;

    private static final FormattedCharSequence LEFT = GeneralUtils.getFormattedChar('>');
    private static final FormattedCharSequence RIGHT = GeneralUtils.getFormattedChar('<');
    private static final FormattedCharSequence ENTITY = GeneralUtils.getFormattedChar('⏺');

    private static final FormattedCharSequence NORTH = GeneralUtils.getFormattedChar('N');
    private static final FormattedCharSequence SOUTH = GeneralUtils.getFormattedChar('S');
    private static final FormattedCharSequence EAST = GeneralUtils.getFormattedChar('E');
    private static final FormattedCharSequence WEST = GeneralUtils.getFormattedChar('W');

    private void calculate(int scaledWidth, int y, float partialTicks, @NotNull LocalPlayer player)
    {
        this.partialTicks = partialTicks;
        this.centerX = scaledWidth / 2;

        this.leftX = centerX - compassScaledWidthHalf;
        this.leftContentX = leftX - 8;

        this.rightX = centerX + compassScaledWidthHalf;
        this.rightContentX = rightX + 8;

        this.y = y;
        this.contentY = y + Constants.LINE_HEIGHT_HALVED;

        this.compassScaledWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) scaledWidth); // All these float are dumb
        this.compassScaledWidthHalf = compassScaledWidth / 2;

        this.yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;
        this.yawInt = (int) yaw;

        this.highlightArea = Math.min(HIGHLIGHT_BOUNDS * zoomAmount * 2, compassScaledWidthHalf);
    }

    public void render(GuiGraphicsExtractor guiGraphics, int scaledWidth, int y, boolean isDummy)
    {
        final LocalPlayer player = mc.player;
        Screen currentScreen = mc.gui.screen();

        if ((currentScreen instanceof PinManageScreen) && !isDummy || player == null)
        {
            return;
        }

        highlightedEntry = null;

        // Calculate compass dimensions
        calculate(scaledWidth, y, guiGraphics.partialTicks(), player);

        // Compass background
        drawBackground(guiGraphics, mc);

        // Check if the entry HUD key is pressed.
        boolean shouldShowNormalHud = !(ClientKeybinds.ENTRY_HUD_KEY.isDown() || currentScreen instanceof SearchPin) && !isDummy;
        if (shouldShowNormalHud)
        {
            // Will be true on the first render pass after entry HUD disappears.
            if (!shouldStartTick)
            {
                EntryManager.stopTickAll(player);
            }

            // Draw all living entities
            if (BritishWeather.getConfig().shouldShowEntitiesOnCompass())
            {
                drawEntities(player, guiGraphics);
            }

            // Render N/E/S/W
            drawCardinal(guiGraphics, 0,   SOUTH);
            drawCardinal(guiGraphics, 90,  WEST);
            drawCardinal(guiGraphics, 180, NORTH);
            drawCardinal(guiGraphics, 270, EAST);

            // Render current heading in degrees if config allows
            drawHeading(guiGraphics);

            // Rendering normal compass now, so set to false so stop tick isn't sent multiple times
            if (trackedPin != null)
            {
                int progressPercentage = trackedPin.getJourney().getProgressFromStart(player.position());

                trackedPin.getRenderer().drawMarker(guiGraphics, rightContentX, -1, false);
                trackedPin.getRenderer().drawProgress(guiGraphics, rightContentX, 1, progressPercentage);

                if (progressPercentage > 100)
                {
                    trackedPin.untrack();
                    this.trackedPin = null;
                }
            }

            shouldStartTick = true;
        } else
        {
            // Draw entry groups and entries.
            drawZoomLevelEffects(guiGraphics, ColorHelper.WHITE);
            drawEntries(guiGraphics, player);
            shouldStartTick = false;
        }

        guiGraphics.verticalLine(centerX, y - 1, y + 7, ColorHelper.CENTER_COLOR);
    }

    private void drawHeading(GuiGraphicsExtractor guiGraphics)
    {
        if (BritishWeather.getConfig().shouldRenderHeading())
        {
            String friendlyDeg = util.getActualDegreesFromYaw(); // Number from 0 to 360
            guiGraphics.text(mc.font, friendlyDeg, util.centerStringAroundX(friendlyDeg, this.centerX), util.getYRowOnCompass(1), ColorHelper.CENTER_COLOR, false);
        }
    }

    private void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc)
    {
        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)
        int decoY = contentY - Constants.LINE_HEIGHT_HALVED;

        guiGraphicsExtractor.horizontalLine(leftX + 1, rightX, contentY - 1, ColorHelper.COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(leftX + 2, rightX, contentY, ColorHelper.COMPASS_BG_SHADOW_COLOUR);

        if (zoomAlertTick > 0)
        {
            int opacity = ColorHelper.getOpacity(ZOOM_ALERT_DURATION, zoomAlertTick, false);
            int colourShiftOpacityWhite = ColorHelper.shiftColourToOpacity(ColorHelper.WHITE, opacity);

            drawZoomLevelEffects(guiGraphicsExtractor, colourShiftOpacityWhite);
        }

        // Draw the two little caps on each end of the compass. Inspired from the God of War 2018 compass.
        guiGraphicsExtractor.text(mc.font, LEFT, leftX - 3, decoY, ColorHelper.COMPASS_BG_COLOR, true);
        guiGraphicsExtractor.text(mc.font, RIGHT, rightX, decoY, ColorHelper.COMPASS_BG_COLOR, true);
    }

    private void drawZoomLevelEffects(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        String amount = String.valueOf(zoomAmount);
        guiGraphicsExtractor.text(mc.font, amount, leftContentX - mc.font.width(amount), util.getYRowOnCompass(0), colour, true);

        int right = centerX + highlightArea;
        if (right < rightX)
        {
            int left = centerX - highlightArea;
            guiGraphicsExtractor.verticalLine(left, y, contentY + 2, ColorHelper.WHITE);
            guiGraphicsExtractor.verticalLine(right, y, contentY + 2, ColorHelper.WHITE);
        }
    }

    private void drawEntities(LocalPlayer player, GuiGraphicsExtractor guiGraphics)
    {
        GeneralUtils.getEntitiesInView(player, LivingEntity.class).forEach(entity -> {
            // Angle from us to the entity
            int xForEntity = util.getXFromPosition(entity.position(), true);
            if (xForEntity == Integer.MAX_VALUE)
            {
                return;
            }

            // Distance form entity. Used for the icon opacity. Further away, more transparent. Closer, more opaque
            int distanceFromEntity = MathHelper.getDistance(player.position(), entity.position());
            // SCALE_MAX is the maximum value iconScale can be. Cannot be over 255 because 255 is white.
            int iconScale = ColorHelper.getOpacity(GeneralUtils.DETECTION_DISTANCE, distanceFromEntity, true);
            int livingEntity = ColorHelper.getColourForEntity(entity, player, iconScale); // Colour entity will be on the compass.

            // Entity x offset on screen. ex will be MAX_VALUE if the entity is off-screen.
            guiGraphics.text(mc.font, ENTITY, xForEntity, y, livingEntity, false);
        });
    }

    /*
    Draw a cardinal direction label at specified angle
     */
    private void drawCardinal(GuiGraphicsExtractor guiGraphics, float angle, FormattedCharSequence label)
    {
        int dx = util.getAngleX(angle);

        if (dx != Integer.MAX_VALUE)
        {
            guiGraphics.text(mc.font, label, dx - 3, y, ColorHelper.WHITE, true);
        }
    }

    private void tickAndRenderGroup(GuiGraphicsExtractor guiGraphicsExtractor, AbstractEntryGroup<? extends AbstractEntry> group, LocalPlayer player)
    {
        if (group.isBuiltIn() && !BritishWeather.getConfig().shouldShowBuiltinObjectives())
        {
            return;
        }

        if (shouldStartTick)
        {
            group.startTick(player);
        }

        group.renderExtra(guiGraphicsExtractor, this);
        group.getEntries().forEach(groupEntry -> groupEntry.getRenderer().draw(guiGraphicsExtractor, player, shouldStartTick, true));
    }
    /*
    Draw all entries and entry groups
     */
    private void drawEntries(GuiGraphicsExtractor guiGraphicsExtractor, LocalPlayer player)
    {
        this.renderMap.clear();

        entriesOnLeftEdge = 0;
        entriesOnRightEdge = 0;

        // Draw entries inside of entry groups.
        Set<AbstractEntryGroup<?>> groupsToCheck = EntryManager.getEntryGroupsInDimension(player.level().dimension());
        Set<AbstractEntryGroup<?>> generalGroups = EntryManager.getEntryGroupsInAll();

        for (AbstractEntryGroup<?> generalGroup : generalGroups)
        {
            tickAndRenderGroup(guiGraphicsExtractor, generalGroup, player);
        }

        for (AbstractEntryGroup<?> group : groupsToCheck)
        {
            tickAndRenderGroup(guiGraphicsExtractor, group, player);
        }

        // Draw normal entries.
        for (AbstractEntry entry : EntryManager.getEntriesInDimension(player.level().dimension()))
        {
            entry.getRenderer().calculateState(this);
            float key = Math.abs(entry.getRenderer().distanceFromHeading);
            this.renderMap.put(key, entry);
        }

        for (@NotNull Iterator<Map.Entry<Float, AbstractEntry>> it = this.renderMap.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<Float, AbstractEntry> entry = it.next();
            AbstractEntry value = entry.getValue();

            if (!it.hasNext() && value.getRenderState() == EntryRenderer.RenderState.HIGHLIGHT)
            {
                highlightedEntry = value;
                highlightedEntry.getRenderer().setRenderState(EntryRenderer.RenderState.FOCUSED);
            }

            value.getRenderer().draw(guiGraphicsExtractor, player, shouldStartTick, false);
        }
    }

    public void displayZoom(int zoomStep)
    {
        int newZoomAmount = zoomAmount + zoomStep;

        if (newZoomAmount < 1)
        {
            zoomAmount = 1;
            return;
        }

        zoomAmount = newZoomAmount;
        zoomAmountSmooth = 1.0f + (zoomAmount - 1.0f) * 0.5f;
        zoomAlertTick = Compass.ZOOM_ALERT_DURATION;

        mc.player.playSound(ClientCommon.SCROLL, 0.04f, 1.5f);
    }

    private void tick()
    {
        zoomAlertTick--;
        boolean isPinHudKeyHeld = ClientKeybinds.ENTRY_HUD_KEY.isDown();

        // Is there a better way to listen for key presses? This is horrible.
        if (isPinHudKeyHeld && (currentHeldKey == null || !currentHeldKey.isDown()))
        {
            if (ClientKeybinds.SNAP_TO_NEAREST.isDown())
            {
                snap();
                currentHeldKey = ClientKeybinds.SNAP_TO_NEAREST;
            }
            else if (ClientKeybinds.CREATE.isDown())
            {
                mc.setScreenAndShow(new PinManageScreen(mc.player, null));
            } else if (ClientKeybinds.FIND.isDown())
            {
                mc.setScreenAndShow(new SearchPin());
            } else if ( ClientKeybinds.TRACK.isDown() ) {
                if (highlightedEntry instanceof PinEntry pin)
                {
                    if (pin.isTracked())
                    {
                        setTrackedPin(null);
                    } else {
                        setTrackedPin(pin);
                    }
                } else {
                    setTrackedPin(null);
                }

                currentHeldKey = ClientKeybinds.TRACK;
            } else if (highlightedEntry != null)
            {
                if (ClientKeybinds.DELETION.isDown())
                {
                    EntryManager.removeEntry(this.highlightedEntry);
                    this.highlightedEntry = null;
                    currentHeldKey = ClientKeybinds.DELETION;
                } else if (ClientKeybinds.DEBUG_ON_PRESS.isDown())
                {
                    Vec3 pos = this.highlightedEntry.getPosition();
                    mc.player.connection.sendCommand("tp @s %s %s %s".formatted(pos.x, pos.y, pos.z));
                } else if (ClientKeybinds.EDIT.isDown()) {
                    if (highlightedEntry instanceof PinEntry pin && pin.canBeEdited())
                    {
                        mc.setScreenAndShow(new PinManageScreen(mc.player, pin));
                    }
                    currentHeldKey = ClientKeybinds.EDIT;

                } else {
                    currentHeldKey = null;
                }
            } else {
                currentHeldKey = null;
            }
        }
    }

    private void snap()
    {
        @Nullable AbstractEntry closest = getClosestEntry();
        if (closest != null)
        {
            mc.player.setYRot(closest.getRenderer().angleFromHeading);
        }
    }

    public void setTrackedPin(@Nullable PinEntry pin)
    {
        if (trackedPin != null)
        {
            trackedPin.untrack();
        }

        this.trackedPin = pin;

        if (trackedPin != null && !pin.isTracked())
        {
            pin.track(mc.player.position());
        }
    }

    private @Nullable AbstractEntry getClosestEntry()
    {
        Map.Entry<Float, AbstractEntry> lowerEntry = renderMap.lowerEntry(4f);
        if (lowerEntry == null)
        {
            return null;
        }

        return lowerEntry.getValue();
    }

    public @Nullable PinEntry getTrackedPin()
    {
        return trackedPin;
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

    public static void addEntry(AbstractEntry provider)
    {
        EntryManager.addEntry(provider);
    }

    public static void tick(@NotNull LocalPlayer player)
    {
        Compass compass = getInstance();

        EntryManager.tick(player);
        compass.tick();
    }

    public static void cleanupAndReset()
    {
        //EntryManager.save();
        EntryManager.clear();

        Compass compass = getInstance();

        compass.highlightedEntry = null;
        compass.trackedPin = null;
        compass.currentHeldKey = null;
    }

    public static void prepareAndLoad()
    {
        EntryManager.clear();
        EntryManager.load();
    }
}
