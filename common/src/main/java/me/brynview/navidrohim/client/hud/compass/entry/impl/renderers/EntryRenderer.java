package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.EntryAnimation;
import me.brynview.navidrohim.client.hud.compass.entry.impl.animations.FadeoutAnimation;
import me.brynview.navidrohim.client.hud.compass.entry.impl.animations.HighlightedAnimation;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

// Might ditch
public class EntryRenderer<T extends AbstractEntry>
{
    private final static int MINIMUM_MARKER_LENGTH = 4;
    private final static float MAX_FOCUS_SCALE = 1.2F;

    public final static int HORIZONTAL_BACKGROUND_MARGINS = 6;
    public final static int VERTICAL_BACKGROUND_MARGINS = 4;
    public final static int TOP_BOTTOM_MARGINS = VERTICAL_BACKGROUND_MARGINS / 2;
    public final static int LEFT_RIGHT_MARGINS = HORIZONTAL_BACKGROUND_MARGINS / 2;

    public enum RenderState
    {
        NONE,
        NORMAL,
        HIGHLIGHT,
        FOCUSED,
        TRACKED
    }

    protected float screenYRow = 0;
    public float distanceFromHeadingDeg;
    public float angleYawFromHeading;
    public float angleTiltFromHeading;

    public int screenX;

    public int renderColour = 0;
    public int backgroundColour = 0;

    protected RenderState renderState;
    protected RenderState oldRenderState;
    protected @Nullable EntryAnimation animation;

    protected final T owner;
    protected final Compass compass;
    protected final Minecraft mc;

    private BlockPos entityVehicleBlockPos = BlockPos.ZERO;
    private String displayDepth = "";

    // animation variables
    private float scale = 1.0F;

    public EntryRenderer(T owner)
    {
        this.owner = owner;
        this.mc = Minecraft.getInstance();
        this.compass = Compass.getInstance();
    }

    public RenderState calculateState(Compass.CompassRenderContext context)
    {
        RenderState state;

        if (renderColour == 0 || backgroundColour == 0)
        {
            renderColour = owner.defaultColour;
            backgroundColour = owner.highlightBgColour;
        }

        boolean shouldDisappear = owner.getSettings().shouldDisappearWhenOOB() && !owner.isTracked();

        Vec3 entryPos = owner.getPosition(context.partialTicks);
        angleYawFromHeading = (float) MathHelper.angleFromPos(entryPos, context.playerPosition);
        angleTiltFromHeading = (float) MathHelper.xAngleFromPos(entryPos, context.playerPosition);
        screenX = context.getAngleX(angleYawFromHeading, shouldDisappear);

        if (screenX == Integer.MAX_VALUE)
        {
            setRenderState(RenderState.NONE);
            return RenderState.NONE;
        }

        boolean isHighlightable = context.isXHighlightable(screenX);
        distanceFromHeadingDeg = Math.abs(Mth.wrapDegrees(context.yaw - angleYawFromHeading));

        if (owner.isTracked())
        {
            trackedState();
            return renderState;
        } else if (isHighlightable)
        {
            state = RenderState.HIGHLIGHT;
        } else {
            state = RenderState.NORMAL;
        }

        this.setRenderState(state);
        return renderState;
    }

    public RenderState getRenderState()
    {
        return renderState;
    }

    public void setRenderState(RenderState newRenderState)
    {
        if (newRenderState == RenderState.FOCUSED && this.oldRenderState != RenderState.FOCUSED)
        {
            onFocus();
        } else if (newRenderState != RenderState.FOCUSED && this.oldRenderState == RenderState.FOCUSED)
        {
            onUnfocus();
        }

        this.oldRenderState = this.renderState;
        this.renderState = newRenderState;
    }

    protected void onFocus()
    {
        // Set flag to start animation somehow
        if (animation != null)
        {
            animation.interrupted();
            animation.reset();
        }
        if (!owner.isTracked())
        {
            animation = new HighlightedAnimation((lerp) -> {
                scale = (float) Mth.lerp(lerp, scale, MAX_FOCUS_SCALE);
                renderColour = ARGB.srgbLerp(lerp.floatValue(), renderColour, owner.highlightColor);
            }, this.owner);
        }
    }

    protected void onUnfocus()
    {
        if (animation != null)
        {
            animation.interrupted();
            animation.reset();
        }

        if (!owner.isTracked())
        {
            animation = new FadeoutAnimation((lerp) -> {
                scale = (float) Mth.lerp(lerp, scale, 1);
                renderColour = ARGB.srgbLerp(lerp.floatValue(), renderColour, owner.defaultColour);
            }, this.owner);
        }
    }

    public void renderTick(@NotNull LocalPlayer ignored)
    {
        // Do check for animaiton tick
        if (animation != null)
        {
            this.animation.tick();
            if (animation.hasFinished())
            {
                animation = null;
            }
        }
    }
    /*
     * Marks this entry as the one the player is pointing at. Must be called after calculateState for the current frame.
     */
    public void focusState()
    {
        setRenderState(RenderState.FOCUSED);
    }

    public void trackedState()
    {
        setRenderState(RenderState.TRACKED);

        renderColour = ColorHelper.OBJECTIVE_MARKER_COLOUR;
        backgroundColour = ColorHelper.getBackgroundColour(renderColour);
    }

    public boolean shouldTakePriority()
    {
        return getRenderState() == RenderState.TRACKED || getRenderState() == RenderState.FOCUSED;
    }

    public void draw(GuiGraphicsExtractor guiGraphicsExtractor, @NotNull LocalPlayer player, boolean shouldStartTick,  Compass.CompassRenderContext context)
    {
        // Check if the entry should be rendered and the entry dimension matches the players current dimension
        if (!owner.shouldRender() || getRenderState() == RenderState.NONE)
        {
            return;
        }

        if (shouldStartTick)
        {
            owner.startTick(player, context);
        }

        drawRawEntry(guiGraphicsExtractor, context);
    }

    protected @NonNull String getDisplayableMarker(AbstractEntry entryRenderable, Compass.CompassRenderContext context, boolean smart, int compassX)
    {
        // Draw entry marker
        String displayableMarker;
        String marker = entryRenderable.getMarker();
        int markerLength = marker.length();

        if (shouldTakePriority() || markerLength <= MINIMUM_MARKER_LENGTH)
        {
            displayableMarker = marker;
        } else if (!smart)
        {
            displayableMarker = marker.substring(0, MINIMUM_MARKER_LENGTH) + "...";
        } else
        {
            int compassHalf = context.compassWidthHalf;

            float markerLengthWithoutBeginning = markerLength - MINIMUM_MARKER_LENGTH;
            int distFromCenter = compassHalf - Math.abs(context.centerX - compassX);
            float perCharAmt = markerLengthWithoutBeginning / compassHalf;
            int showLen = (int) Math.clamp(Math.min(MINIMUM_MARKER_LENGTH + (perCharAmt * distFromCenter), markerLength), MINIMUM_MARKER_LENGTH, 255);

            if (compassX < context.centerX) // left side
            {
                String baseMarker = marker.substring(0, MINIMUM_MARKER_LENGTH);
                String displayableRest = marker.substring(MINIMUM_MARKER_LENGTH, showLen);
                displayableMarker = baseMarker + displayableRest + "...";
            } else
            {
                String baseMarker = marker.substring(markerLength - MINIMUM_MARKER_LENGTH, markerLength); // This part of the marker will always be visible. A preview basically.
                String displayableRest = marker.substring((markerLength - showLen), markerLength - MINIMUM_MARKER_LENGTH);
                displayableMarker = "..." + displayableRest + baseMarker;
            }
        }
        return displayableMarker;
    }

    public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context)
    {
        drawMarker(guiGraphicsExtractor, context, screenX, screenYRow - 1);
        if (shouldTakePriority())
        {
            drawDecoration(guiGraphicsExtractor, context);
        }
        drawDistance(guiGraphicsExtractor, context);
        drawDebug(guiGraphicsExtractor, context);
    }

    private void drawDebug(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context)
    {
        if (BritishWeather.getConfig().debug() && shouldTakePriority())
        {
            guiGraphicsExtractor.centeredText(context.font, this.owner.debugString(), screenX, context.getYRowOnCompass(screenYRow + 2), renderColour);
        }
    }

    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext, int x, float yRow)
    {
        int renderY = compassRenderContext.getYRowOnCompass(yRow, MAX_FOCUS_SCALE) - 1;
        drawMarker(guiGraphicsExtractor, compassRenderContext, x, renderY, true);
    }

    public int drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, @Nullable Compass.CompassRenderContext compassRenderContext, int x, int y, boolean shouldCenterX)
    {
        String displayableMarker = compassRenderContext != null ? getDisplayableMarker(owner, compassRenderContext, owner.getMarker().length() <= 15, screenX) : owner.getMarker();

        int markerWidth = mc.font.width(displayableMarker);
        int centeredX = shouldCenterX ? MathHelper.centerWidthAroundX((int) (markerWidth * scale), x) : x;

        Matrix3x2fStack pose = guiGraphicsExtractor.pose();

        pose.pushMatrix();
        pose.translate(centeredX, y);
        pose.scale(scale, scale);

        if (shouldTakePriority() && BritishWeather.getConfig().shouldShowPinBackground() && owner.getSettings().shouldShowBackground())
        {
            drawBackground(guiGraphicsExtractor, 0, 0, markerWidth);
        }

        guiGraphicsExtractor.text(mc.font, displayableMarker, 0, 0, renderColour, owner.getSettings().shouldShowShadow());
        pose.popMatrix();

        return markerWidth;
    }

    public void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, int markerWidth)
    {
        guiGraphicsExtractor.fill(x - TOP_BOTTOM_MARGINS, y - TOP_BOTTOM_MARGINS, x + markerWidth + TOP_BOTTOM_MARGINS, y + mc.font.lineHeight, backgroundColour);
        guiGraphicsExtractor.outline(x - LEFT_RIGHT_MARGINS, y - LEFT_RIGHT_MARGINS, markerWidth + HORIZONTAL_BACKGROUND_MARGINS, mc.font.lineHeight + VERTICAL_BACKGROUND_MARGINS, renderColour);
    }

    public void drawProgressAndInfo(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext, int x, int yRow, int progress)
    {
        String displayable = String.valueOf(progress);

        if (progress >= 100)
        {
            displayable += "% (arrived)";
        } else if (progress < 0)
        {
            displayable = "Off course";
        } else {
            displayable += "%";
        }

        displayable += " :: " + compassRenderContext.getDisplayableSpeed();
        if (compassRenderContext.player.getVehicle() instanceof AbstractBoat boat)
        {
            if (!entityVehicleBlockPos.equals(boat.blockPosition()))
            {
                entityVehicleBlockPos = boat.blockPosition();
                displayDepth = compassRenderContext.getDepthBelow(boat);
            }
            displayable += " :: " + displayDepth;
        }
        guiGraphicsExtractor.text(mc.font, displayable, x, compassRenderContext.getYRowOnCompass(yRow), renderColour);
    }

    public void drawDistance(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext)
    {
        // If in view, render the distance from entry and if the player should go up or down to reach it
        Vec3 playerPos = compassRenderContext.playerPosition;
        if (owner.getSettings().shouldShowDistance() && (shouldTakePriority() || owner.isBuiltIn()))
        {
            int distance = MathHelper.getDistance(owner.getPosition(guiGraphicsExtractor.partialTicks()), playerPos);

            String suffix = "m";
            double heightDiff = playerPos.y - owner.getPosition().y;
            // Both indicators check if the player is within 200 blocks, if not, just ignore.
            if (distance <= 200)
            {
                if (heightDiff >= 3) // down
                {
                    suffix += " ↓";
                } else if (heightDiff <= -3) // up
                {
                    suffix += " ↑";
                } else if (heightDiff == 0)
                {
                    suffix += " -";
                }
            }

            // Draw distance from entry
            String distanceFromObjective = MathHelper.getDistance(playerPos, owner.getPosition(guiGraphicsExtractor.partialTicks())) + suffix + owner.getExtraInformation();
            int xOnCompass = compassRenderContext.centerStringAroundX(distanceFromObjective, screenX);

            guiGraphicsExtractor.text(mc.font, distanceFromObjective, xOnCompass, compassRenderContext.getYRowOnCompass(screenYRow + 1), renderColour, owner.getSettings().shouldShowShadow());
        }
    }

    public void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext)
    {
        guiGraphicsExtractor.verticalLine(screenX, compassRenderContext.getYRowOnCompass(screenYRow) - 2, compassRenderContext.getYRowOnCompass(screenYRow + 0.5), renderColour);
    }

    public int getX()
    {
        return screenX;
    }
}
