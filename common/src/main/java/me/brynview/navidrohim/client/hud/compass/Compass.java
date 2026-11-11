package me.brynview.navidrohim.client.hud.compass;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.keybinds.KeyHandler;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.client.screen.PinManageScreen;
import me.brynview.navidrohim.client.screen.PinSeachScreen;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.brynview.navidrohim.util.GeneralUtils.findOceanFloor;

public class Compass
{
    public Compass()
    {
        mc = Minecraft.getInstance();
    }

    public static final class CompassRenderContext
    {
        private static final float BASE_VISIBLE_AMOUNT = 180F;

        // Yaw at the current render pass
        public final float yaw;
        public final Vec3 playerPosition;

        // Compass width scaled with the compass size config value
        private final int compassWidth;
        public final int compassWidthHalf;

        // x coordinate of the compass. Can change due to scaling
        public final int centerX;

        public final int leftX;
        public final int leftContentX;

        public final int rightX;
        public final int rightContentX;

        public final int y;
        public final int contentY;

        public final @NotNull LocalPlayer player;
        public final @NotNull Font font;

        private @Nullable String displayableSpeed = null;
        private @Nullable String displayableSpeedVertical = null;

        public final float partialTicks;
        private final int highlightArea;
        private final float zoom;

        public final boolean isTracking;

        public CompassRenderContext(int width, int y, float partialTick, float yaw, final @NotNull LocalPlayer player, final Compass compass, float zoom, boolean isTracking)
        {
            this.isTracking = isTracking;
            this.player = player;
            this.font = compass.mc.font;
            this.compassWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) width); // All these float are dumb
            this.compassWidthHalf = compassWidth / 2;
            this.zoom = zoom;

            this.partialTicks = partialTick;
            this.centerX = width / 2;

            this.leftX = centerX - compassWidthHalf;
            this.leftContentX = leftX - 8;

            this.rightX = centerX + compassWidthHalf;
            this.rightContentX = rightX + 13;

            this.y = y;
            this.contentY = y + Constants.LINE_HEIGHT_HALVED;

            this.yaw = yaw;
            this.playerPosition = player.getPosition(partialTicks);

            this.highlightArea = Math.min(HIGHLIGHT_BOUNDS * compass.displayZoom * 2, compassWidthHalf);
        }

        public String calculateDisplayableSpeed(Entity speedForEntity, boolean includeVerticalVector)
        {
            double speedBlocksPerTick;
            if (!includeVerticalVector)
            {
                speedBlocksPerTick = speedForEntity.getDeltaMovement().horizontalDistance();
            } else
            {
                speedBlocksPerTick = speedForEntity.getDeltaMovement().length();
            }

            double speedBlocksPerSecond = speedBlocksPerTick * 20;

            return speedBlocksPerSecond <= 100D ? (int) speedBlocksPerSecond + "m/s" : "Slow down mate.";
        }

        public String getDisplayableSpeed()
        {
            if (displayableSpeed == null)
            {
                this.displayableSpeed = calculateDisplayableSpeed(player.getVehicle() == null ? player : player.getVehicle(), false);
            }

            return displayableSpeed;
        }

        public String getDepthBelow(Entity entity)
        {
            return ((int) playerPosition.y) - findOceanFloor(entity) + "m";
        }

        public String getDisplayableSpeedVertical()
        {
            if (displayableSpeedVertical == null)
            {
                this.displayableSpeedVertical = calculateDisplayableSpeed(player.getVehicle() == null ? player : player.getVehicle(), true);
            }

            return displayableSpeedVertical;
        }

        public String getActualDegreesFromYaw()
        {
            return (Mth.wrapDegrees((int) yaw) + 180) % 360 + "°";
        }

        public int centerStringAroundX(String str, int xPlacement)
        {
            return centerWidthAroundX(font.width(str), xPlacement);
        }

        public int centerWidthAroundX(int width, int x)
        {
            return x - width / 2;
        }

        public boolean isXOutOfBounds(int x)
        {
            return x == Integer.MAX_VALUE;
        }

        public boolean isXHighlightable(int x)
        {
            return x <= centerX + highlightArea && x >= centerX - highlightArea;
        }

        private int getAngleX(float angle)
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
            float amountOnCompass = BASE_VISIBLE_AMOUNT / zoom;
            double aDist = Mth.wrapDegrees(angle - yaw) * (compassWidth / amountOnCompass);
            double csx = centerX + aDist;

            // Make sure text is not rendered past the compass bounds
            if (Math.abs(aDist) < compassWidthHalf)
            {
                return Math.toIntExact(Math.round(csx)); // x will be center of the screen. aDist is the offset where to render text
            }

            // If element should stay on right edge of compass
            if (!shouldDisappearWhenOOB && (aDist >= compassWidthHalf))
            {
                return centerX + compassWidthHalf;
            } else if (!shouldDisappearWhenOOB && aDist <= -compassWidthHalf) // Same but left edge
            {
                return centerX - compassWidthHalf;
            }

            // Return max int to make sure whatever is going out of bounds fucks right off so we don't see it
            return Integer.MAX_VALUE;
        }

        // Y funcs

        public int getYRowOnCompass(double row)
        {
            // Could this be cached? And if so, would it be faster?
            return (int) (y + (font.lineHeight * row));
        }

        public int getYOnRowCentered(int row, int height)
        {
            return getYRowOnCompass(row) - (height / 4);
        }

        public int getXFromPosition(Vec3 position, boolean disappearWhenOutOfBounds)
        {
            double angleRelativeToPosition = MathHelper.angleFromPos(position, playerPosition);
            return getAngleX(angleRelativeToPosition, disappearWhenOutOfBounds);
        }
    }

    // Text used for compass decorations
    private static final int ZOOM_ALERT_DURATION = 40;
    private static final int HIGHLIGHT_BOUNDS = 7;

    private static Compass INSTANCE;

    // Non-final general variables

    public CompassRenderContext context;
    public Minecraft mc;

    private int zoomAlertTick = 0;
    private int displayZoom = 1;
    public float zoomAmount = 1;

    // Yaw smoothing. Higher zoom spreads each degree over more pixels, so the compass is eased to stop it feeling twitchy.
    private static final float YAW_SMOOTHING_PER_ZOOM = 0.02F; // Seconds of easing per zoom step above 1x
    private static final long YAW_SNAP_AFTER_NANOS = 250_000_000L; // If not drawn for this long, snap instead of swinging round

    private float displayYaw; // Our current actual yaw
    private long lastYawUpdateNanos = 0L;

    public @Nullable PinEntry trackedPin;
    public @Nullable AbstractEntry highlightedEntry;
    private List<LivingEntity> entitiesNearPlayer = new ArrayList<>();

    private boolean shouldStartTick = false;

    private static final FormattedCharSequence LEFT = GeneralUtils.getFormattedChar('>');
    private static final FormattedCharSequence RIGHT = GeneralUtils.getFormattedChar('<');
    private static final FormattedCharSequence ENTITY = GeneralUtils.getFormattedChar('⏺');

    private static final FormattedCharSequence NORTH = GeneralUtils.getFormattedChar('N');
    private static final FormattedCharSequence SOUTH = GeneralUtils.getFormattedChar('S');
    private static final FormattedCharSequence EAST = GeneralUtils.getFormattedChar('E');
    private static final FormattedCharSequence WEST = GeneralUtils.getFormattedChar('W');

    public void render(GuiGraphicsExtractor guiGraphics, int scaledWidth, int y, boolean isDummy)
    {
        final LocalPlayer player = mc.player;
        Screen currentScreen = mc.gui.screen();

        if ((currentScreen instanceof PinManageScreen) && !isDummy || player == null || !BritishWeather.getConfig().shouldRenderCompass())
        {
            return;
        }

        highlightedEntry = null;

        // Calculate compass dimensions
        float partialTicks = guiGraphics.partialTicks();
        float yaw = smoothYaw(player.getYRot(partialTicks));
        boolean compassView = !(KeyHandler.ENTRY_HUD_KEY.isDown() || currentScreen instanceof PinSeachScreen) && !isDummy;

        context = new CompassRenderContext(scaledWidth, y, partialTicks, yaw, player, this, zoomAmount, trackedPin != null);

        // Compass background
        drawBackground(guiGraphics, mc);

        // Check if the entry HUD key is pressed.
        if (compassView)
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
            drawCardinal(guiGraphics, 0, SOUTH);
            drawCardinal(guiGraphics, 90, WEST);
            drawCardinal(guiGraphics, 180, NORTH);
            drawCardinal(guiGraphics, 270, EAST);

            // Render current heading in degrees if config allows
            drawHeading(guiGraphics, context);

            // Rendering normal compass now, so set to false so stop tick isn't sent multiple times
            shouldStartTick = true;
        } else
        {
            // Draw entry groups and entries.
            drawZoomLevelEffects(guiGraphics, ColorHelper.WHITE);
            if (!isDummy)
            {
                drawEntries(guiGraphics, player);
            }
            drawElytraSpeed(guiGraphics);

            if (BritishWeather.getConfig().shouldShowKeyHelper())
            {
                KeyHandler.extractKeyHelper(guiGraphics, context);
            }

            shouldStartTick = false;
        }

        if (trackedPin != null && trackedPin.getLevel().equals(GeneralUtils.getDimensionFromPlayer(player)))
        {
            if (trackedPin.hasExpired())
            {
                trackedPin = null;
            } else
            {
                trackedPin.getRenderer().calculateState(context);
                trackedPin.getRenderer().drawMarker(guiGraphics, context, context.rightContentX, -1, false);

                int progressPercentage = (int) trackedPin.getJourney().getJourneyProgress(context.playerPosition);
                trackedPin.getRenderer().drawProgressAndInfo(guiGraphics, context, context.rightContentX, 1, progressPercentage);

                if (compassView)
                {
                    guiGraphics.text(mc.font, ENTITY, trackedPin.getRenderer().getX(), context.y, trackedPin.getRenderer().renderColour);
                } else
                {
                    trackedPin.getRenderer().drawDecoration(guiGraphics, context);
                    trackedPin.getRenderer().drawMarker(guiGraphics, context, trackedPin.getRenderer().screenX, -1);
                }
            }
        }

        guiGraphics.verticalLine(context.centerX, y - 1, y + 7, ColorHelper.CENTER_COLOR);
    }

    private void drawElytraSpeed(GuiGraphicsExtractor guiGraphics)
    {
        if (context.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) && context.player.isFallFlying() && trackedPin == null)
        {
            guiGraphics.text(context.font, context.getDisplayableSpeed(), context.rightContentX, context.getYRowOnCompass(0), ColorHelper.WHITE);
        }
    }

    private void drawHeading(GuiGraphicsExtractor guiGraphics, CompassRenderContext context)
    {
        if (BritishWeather.getConfig().shouldRenderHeading())
        {
            String friendlyDeg = context.getActualDegreesFromYaw(); // Number from 0 to 360
            guiGraphics.text(mc.font, friendlyDeg, context.centerStringAroundX(friendlyDeg, context.centerX), context.getYRowOnCompass(1), ColorHelper.CENTER_COLOR, false);
        }
    }

    private void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc)
    {
        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)
        int decoY = context.contentY - Constants.LINE_HEIGHT_HALVED;

        guiGraphicsExtractor.horizontalLine(context.leftX + 1, context.rightX, context.contentY - 1, ColorHelper.COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(context.leftX + 2, context.rightX, context.contentY, ColorHelper.COMPASS_BG_SHADOW_COLOUR);

        if (zoomAlertTick > 0)
        {
            int opacity = ColorHelper.getOpacity(ZOOM_ALERT_DURATION, zoomAlertTick, false);
            int colourShiftOpacityWhite = ColorHelper.shiftColourToOpacity(ColorHelper.WHITE, opacity);

            drawZoomLevelEffects(guiGraphicsExtractor, colourShiftOpacityWhite);
        }

        // Draw the two little caps on each end of the compass. Inspired from the God of War 2018 compass.
        guiGraphicsExtractor.text(mc.font, LEFT, context.leftX - 3, decoY, ColorHelper.COMPASS_BG_COLOR, true);
        guiGraphicsExtractor.text(mc.font, RIGHT, context.rightX, decoY, ColorHelper.COMPASS_BG_COLOR, true);
    }

    private void drawZoomLevelEffects(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        String amount = String.valueOf(displayZoom);
        guiGraphicsExtractor.text(mc.font, amount, context.leftContentX - mc.font.width(amount), context.getYRowOnCompass(0), colour, true);

        int right = context.centerX + context.highlightArea;
        if (right < context.rightX)
        {
            int left = context.centerX - context.highlightArea;
            guiGraphicsExtractor.verticalLine(left, context.y, context.contentY + 2, ColorHelper.WHITE);
            guiGraphicsExtractor.verticalLine(right, context.y, context.contentY + 2, ColorHelper.WHITE);
        }
    }

    private void drawEntities(LocalPlayer player, GuiGraphicsExtractor guiGraphics)
    {
        entitiesNearPlayer.forEach(entity -> {

            if (entity.isRemoved())
            {
                return;
            }

            // Angle from us to the entity
            Vec3 entityPosLerp = entity.getPosition(guiGraphics.partialTicks());
            int xForEntity = context.getXFromPosition(entityPosLerp, true);

            if (xForEntity == Integer.MAX_VALUE)
            {
                return;
            }

            // Is this dumb?
            if (entity instanceof Player playerEntity && context.player.connection.getPlayerInfo(playerEntity.getUUID()) instanceof PlayerInfo info)
            {
                PlayerFaceExtractor.extractRenderState(guiGraphics, info.getSkin(), xForEntity, context.y, 8);
            } else
            {
                // Distance form entity. Used for the icon opacity. Further away, more transparent. Closer, more opaque
                int distanceFromEntity = MathHelper.getDistance(context.playerPosition, entityPosLerp);

                // SCALE_MAX is the maximum value iconScale can be. Cannot be over 255 because 255 is white.
                int iconScale = ColorHelper.getOpacity(GeneralUtils.DETECTION_DISTANCE, distanceFromEntity, true);
                int livingEntity = ColorHelper.getColourForEntity(entity, player, iconScale); // Colour entity will be on the compass.

                // Entity x offset on screen. ex will be MAX_VALUE if the entity is off-screen.
                guiGraphics.text(mc.font, ENTITY, xForEntity, context.y, livingEntity, false);
            }
        });
    }

    /*
    Draw a cardinal direction label at specified angle
     */
    private void drawCardinal(GuiGraphicsExtractor guiGraphics, float angle, FormattedCharSequence label)
    {
        int dx = context.getAngleX(angle);

        if (dx != Integer.MAX_VALUE)
        {
            guiGraphics.text(mc.font, label, dx - 3, context.y, ColorHelper.WHITE, true);
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

        group.renderExtra(guiGraphicsExtractor, context);
        group.getEntries().forEach(groupEntry ->
                {
                    groupEntry.getRenderer().calculateState(context);
                    groupEntry.getRenderer().draw(guiGraphicsExtractor, player, shouldStartTick, context);
                }
        );
    }

    /*
    Draw all entries and entry groups
     */
    private void drawEntries(GuiGraphicsExtractor guiGraphicsExtractor, LocalPlayer player)
    {
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
        Collection<? extends AbstractEntry> entries = EntryManager.getEntriesInDimension(player.level().dimension());
        AbstractEntry closest = null;

        // Pass 1: calculate state for every entry and find the highlightable entry closest to the heading.
        for (AbstractEntry entry : entries)
        {
            EntryRenderer<?> renderer = entry.getRenderer();
            if (renderer.calculateState(context) == EntryRenderer.RenderState.HIGHLIGHT
                    && (closest == null || renderer.distanceFromHeading < closest.getRenderer().distanceFromHeading))
            {
                closest = entry;
            }
        }

        if (closest != null)
        {
            highlightedEntry = closest;
            closest.getRenderer().focus();
        }

        // Pass 2: draw everything else, then the focused entry last so it sits on top.
        for (AbstractEntry entry : entries)
        {
            if (entry != closest)
            {
                entry.getRenderer().draw(guiGraphicsExtractor, player, shouldStartTick, context);
            }
        }

        if (closest != null)
        {
            closest.getRenderer().draw(guiGraphicsExtractor, player, shouldStartTick, context);
        }
    }

    /*
     * Eases the displayed yaw towards the player's yaw. Instant at 1x zoom, slower the further the compass is zoomed.
     * Uses real frame time so the speed is the same regardless of FPS.
     */
    private float smoothYaw(float targetYaw)
    {
        long now = System.nanoTime();
        long elapsed = now - lastYawUpdateNanos;
        lastYawUpdateNanos = now;

        float timeConstant = YAW_SMOOTHING_PER_ZOOM * (zoomAmount - 1F);
        if (timeConstant <= 0F || elapsed > YAW_SNAP_AFTER_NANOS)
        {
            displayYaw = targetYaw;
            return displayYaw;
        }

        float alpha = 1F - (float) Math.exp(-(elapsed / 1_000_000_000D) / timeConstant);
        displayYaw += Mth.wrapDegrees(targetYaw - displayYaw) * alpha; // wrapDegrees so it takes the short way round
        return displayYaw;
    }

    public void displayZoom(int zoomStep)
    {
        int newZoomAmount = displayZoom + zoomStep;

        if (newZoomAmount < 1)
        {
            displayZoom = 1;
            return;
        }

        displayZoom = newZoomAmount;
        zoomAmount = 1.0f + (displayZoom - 1.0f) * 0.5f;
        zoomAlertTick = Compass.ZOOM_ALERT_DURATION;

        context.player.playSound(ClientCommon.SCROLL, 0.04f, 1.5f);
    }

    public void tick(@NotNull LocalPlayer player)
    {
        if (!BritishWeather.getConfig().shouldRenderCompass())
        {
            return;
        }

        if (zoomAlertTick % 20 == 0)
        {
            entitiesNearPlayer = GeneralUtils.getEntitiesInView(player, LivingEntity.class);
        }

        EntryManager.tick(player);
        zoomAlertTick--;
    }

    public void delete()
    {
        if (highlightedEntry != null)
        {
            EntryManager.removeEntry(this.highlightedEntry);
            this.highlightedEntry = null;
        } else
        {
            notHighlightedWarning();
        }
    }

    public void newPin()
    {
        GeneralUtils.setScreenAndShowNoFlicker(new PinManageScreen(context.player, null));
    }

    public void editPin()
    {
        if (highlightedEntry != null && highlightedEntry instanceof PinEntry pin && pin.canBeEdited())
        {
            GeneralUtils.setScreenAndShowNoFlicker(new PinManageScreen(context.player, pin));
        } else
        {
            notHighlightedWarning();
        }
    }

    public void find()
    {
        GeneralUtils.setScreenAndShowNoFlicker(new PinSeachScreen(context.player.level().dimension()));
    }

    public void teleport()
    {
        if (highlightedEntry != null)
        {
            Vec3 pos = this.highlightedEntry.getPosition();
            context.player.connection.sendCommand("tp @s %s %s %s".formatted(pos.x, pos.y, pos.z));
        } else
        {
            notHighlightedWarning();
        }
    }

    public void track()
    {
        if (highlightedEntry instanceof PinEntry pin)
        {
            if (pin.isTracked())
            {
                setTrackedPin(null);
            } else
            {
                setTrackedPin(pin);
            }
        } else if (trackedPin == null)
        {
            notHighlightedWarning();
        } else
        {
            setTrackedPin(null);
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
            pin.track(context.player.position());
            context.player.sendOverlayMessage(Component.translatable("br.dialogue.tracking_marker", pin.getMarker()));
        }
    }

    public @Nullable PinEntry getTrackedPin()
    {
        return trackedPin;
    }

    private void notHighlightedWarning()
    {
        context.player.sendOverlayMessage(Component.translatable("br.dialogue.not_highlighted"));
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

    public static void cleanupAndReset()
    {
        EntryManager.savePins();
        EntryManager.clear();

        Compass compass = getInstance();

        compass.highlightedEntry = null;
        compass.trackedPin = null;
    }

    public static void prepareAndLoad()
    {
        EntryManager.clear();
        EntryManager.loadPins();
    }
}
