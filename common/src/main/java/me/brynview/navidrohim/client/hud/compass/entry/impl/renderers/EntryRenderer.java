package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

// Might ditch
public class EntryRenderer<T extends AbstractEntry>
{
    private final static int MINIMUM_MARKER_LENGTH = 4;

    public enum RenderState
    {
        NONE,
        NORMAL,
        HIGHLIGHT,
        FOCUSED,
        TRACKED
    }

    protected int screenYRow = 0;
    public float distanceFromHeading;
    public float angleFromHeading;
    public int screenX;

    public int renderColour;
    public int backgroundColour;

    protected RenderState renderState;
    protected final T owner;
    protected final Compass compass;
    protected final Minecraft mc;

    private BlockPos entityVehicleBlockPos = BlockPos.ZERO;
    private String displayDepth = "";

    public EntryRenderer(T owner)
    {
        this.owner = owner;
        this.mc = Minecraft.getInstance();
        this.compass = Compass.getInstance();
    }

    public RenderState calculateState(Compass.CompassRenderContext context)
    {
        RenderState state = RenderState.NORMAL;
        renderColour = owner.defaultColour;
        backgroundColour = owner.highlightBgColour;

        boolean shouldDisappear = owner.getSettings().shouldDisappearWhenOOB() && !owner.isTracked();
        angleFromHeading = (float) MathHelper.angleFromPos(owner.getPosition(context.partialTicks), context.playerPosition);
        screenX = context.getAngleX(angleFromHeading, shouldDisappear);

        if (context.isXOutOfBounds(screenX))
        {
            setRenderState(RenderState.NONE);
            return RenderState.NONE;
        }

        boolean isHighlightable = context.isXHighlightable(screenX) && !owner.isBuiltIn();
        distanceFromHeading = Math.abs(Mth.wrapDegrees(context.yaw - angleFromHeading));

        if (owner.isTracked())
        {
            trackedState();
            return renderState;
        } else if (isHighlightable)
        {
            state = RenderState.HIGHLIGHT;
        }

        this.setRenderState(state);

        return renderState;
    }

    public RenderState getRenderState()
    {
        return renderState;
    }

    public void setRenderState(RenderState renderState)
    {
        this.renderState = renderState;
    }

    /*
     * Marks this entry as the one the player is pointing at. Must be called after calculateState for the current frame.
     */
    public void focus()
    {
        setRenderState(RenderState.FOCUSED);
        renderColour = owner.highlightColor;
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
            owner.startTick(player);
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
            guiGraphicsExtractor.centeredText(context.font, this.owner.debugString(), screenX, context.getYRowOnCompass(2), renderColour);
        }
    }

    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext, int x, int yRow, boolean centerAroundX)
    {
        String displayableMarker = getDisplayableMarker(owner, compassRenderContext, owner.getMarker().length() <= 15, screenX);

        int markerWidth = mc.font.width(displayableMarker);
        int renderX = centerAroundX ? compassRenderContext.centerWidthAroundX(markerWidth, x) : x + 5;
        int renderY = compassRenderContext.getYRowOnCompass(yRow) - 1;

        drawBackground(guiGraphicsExtractor, renderColour, renderX, renderY, markerWidth);
        guiGraphicsExtractor.text(mc.font, displayableMarker, renderX, renderY, renderColour, owner.getSettings().shouldShowShadow());
    }

    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context, int x, int yRow)
    {
        drawMarker(guiGraphicsExtractor, context, x, yRow, true);
    }

    public void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int colour, int x, int y, int markerWidth)
    {
        if (shouldTakePriority() && BritishWeather.getConfig().shouldShowPinBackground() && owner.getSettings().shouldShowBackground())
        {
            guiGraphicsExtractor.fill(x - 2, y - 2, x + markerWidth + 2, y + mc.font.lineHeight, backgroundColour);
            guiGraphicsExtractor.outline(x - 3, y - 3, markerWidth + 5, mc.font.lineHeight + 4, colour);
        }
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
            if (entityVehicleBlockPos.equals(boat.blockPosition()))
            {
                displayable += " :: " + displayDepth;
            } else {
                entityVehicleBlockPos = boat.blockPosition();
                displayDepth = compassRenderContext.getDepthBelow(boat);
                displayable += " :: " + displayDepth;
            }
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
            String distanceFromObjective = MathHelper.getDistance(playerPos, owner.getPosition(guiGraphicsExtractor.partialTicks())) + suffix;
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
