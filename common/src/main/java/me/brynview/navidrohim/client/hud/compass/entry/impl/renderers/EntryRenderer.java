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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

// Might ditch
public class EntryRenderer<T extends AbstractEntry>
{
    public static final int ENTRIES_ALLOWED_ON_SIDE = 7;
    private final static int MINIMUM_MARKER_LENGTH = 4;

    public enum RenderState
    {
        NONE,
        NORMAL,
        HIGHLIGHT,
        FOCUSED,
        TRACKED
    }

    protected int screenX;
    protected int screenYRow = 0;
    public float distanceFromHeading;
    public float angleFromHeading;

    public int renderColourForState;

    protected RenderState renderState;
    protected final T owner;
    protected final Compass compass;
    protected final Minecraft mc;

    public EntryRenderer(T owner)
    {
        this.owner = owner;
        this.mc = Minecraft.getInstance();
        this.compass = Compass.getInstance();
    }

    public void calculateState(Compass compass)
    {
        RenderState state = RenderState.NORMAL;
        renderColourForState = owner.defaultColour;

        boolean shouldDisappear = owner.getSettings().shouldDisappearWhenOOB() && !owner.isTracked();

        angleFromHeading = (float) MathHelper.angleFromPos(owner.getPosition(), mc.player.position());
        distanceFromHeading = Math.abs(Mth.wrapDegrees(compass.yawInt - angleFromHeading));
        screenX = compass.util.getAngleX(angleFromHeading, shouldDisappear);

        boolean isHighlightable = compass.util.isXHighlightable(screenX) && !owner.isBuiltIn();
        owner.isOnLeftEdge = screenX == compass.leftX;
        owner.isOnRightEdge = screenX == compass.rightX;

        if (owner.isTracked())
        {
            this.setRenderState(RenderState.TRACKED);
            renderColourForState = ColorHelper.OBJECTIVE_MARKER_COLOUR;

            return;
        } else if (isHighlightable)
        {
            state = RenderState.HIGHLIGHT;
        } else if (screenX == Integer.MAX_VALUE || (owner.isOnLeftEdge && compass.entriesOnLeftEdge >= ENTRIES_ALLOWED_ON_SIDE) || (owner.isOnRightEdge && compass.entriesOnRightEdge >= ENTRIES_ALLOWED_ON_SIDE))
        {
            this.setRenderState(RenderState.NONE);
            return;
        }

        this.setRenderState(state);

        if (owner.isOnLeftEdge)
        {
            compass.entriesOnLeftEdge++;
        }

        if (owner.isOnRightEdge)
        {
            compass.entriesOnRightEdge++;
        }
    }

    public int getRenderColour()
    {
        return renderColourForState;
    }

    public RenderState getRenderState()
    {
        return renderState;
    }

    public void setRenderState(RenderState renderState)
    {
        this.renderState = renderState;
        if (renderState == RenderState.FOCUSED)
        {
            renderColourForState = owner.getHighlightColor();
        }
    }

    public boolean shouldTakePriority()
    {
        return getRenderState() == RenderState.TRACKED || getRenderState() == RenderState.FOCUSED;
    }
    public void draw(GuiGraphicsExtractor guiGraphicsExtractor, @NotNull LocalPlayer player, boolean shouldStartTick, boolean calculate)
    {
        // Check if the entry should be rendered and the entry dimension matches the players current dimension
        if (calculate)
        {
            calculateState(compass);
        }

        if (!owner.shouldRender() || getRenderState() == RenderState.NONE)
        {
            return;
        }

        if (shouldStartTick)
        {
            owner.startTick(player);
        }

        if (compass.util.isXOutOfBounds(screenX))
        {
            return;
        }

        drawRawEntry(guiGraphicsExtractor);
    }

    protected @NonNull String getDisplayableMarker(AbstractEntry entryRenderable, boolean smart, int compassX)
    {
        // Draw entry marker
        Compass compass = Compass.getInstance();
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
            int compassHalf = compass.compassScaledWidthHalf;

            float markerLengthWithoutBeginning = markerLength - MINIMUM_MARKER_LENGTH;
            int distFromCenter = compassHalf - Math.abs(compass.centerX - compassX);
            float perCharAmt = markerLengthWithoutBeginning / compassHalf;
            int showLen = (int) Math.clamp(Math.min(MINIMUM_MARKER_LENGTH + (perCharAmt * distFromCenter), markerLength), MINIMUM_MARKER_LENGTH, 255);

            if (compassX < compass.centerX) // left side
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

    public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor)
    {
        drawMarker(guiGraphicsExtractor, screenX, screenYRow - 1);
        if (shouldTakePriority())
        {
            drawDecoration(guiGraphicsExtractor);
        }
        drawDistance(guiGraphicsExtractor);
    }

    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, int x, int yRow, boolean centerAroundX)
    {
        String displayableMarker = getDisplayableMarker(owner, owner.getMarker().length() <= 15, screenX);

        int markerWidth = mc.font.width(displayableMarker);
        int renderX = centerAroundX ? compass.util.centerWidthAroundX(markerWidth, x) : x + 5;
        int renderY = compass.util.getYRowOnCompass(yRow) - 1;

        drawBackground(guiGraphicsExtractor, renderColourForState, renderX, renderY, markerWidth);
        guiGraphicsExtractor.text(mc.font, displayableMarker, renderX, renderY, renderColourForState, owner.getSettings().shouldShowShadow());
    }

    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, int x, int yRow)
    {
        drawMarker(guiGraphicsExtractor, x, yRow, true);
    }

    public void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int colour, int x, int y, int markerWidth)
    {
        if (shouldTakePriority() && BritishWeather.getConfig().shouldShowPinBackground() && owner.getSettings().shouldShowBackground())
        {
            guiGraphicsExtractor.fill(x - 2, y - 2, x + markerWidth + 2, y + mc.font.lineHeight, owner.highlightBgColour);
            guiGraphicsExtractor.outline(x - 3, y - 3, markerWidth + 5, mc.font.lineHeight + 4, colour);
        }
    }

    public void drawProgress(GuiGraphicsExtractor guiGraphicsExtractor, int x, int yRow, int progress)
    {
        String displayable = progress + (progress >= 100 ? "% (reached)" : "%");
        guiGraphicsExtractor.text(mc.font, displayable, x, compass.util.getYRowOnCompass(yRow), renderColourForState);
    }

    public void drawDistance(GuiGraphicsExtractor guiGraphicsExtractor)
    {
        // If in view, render the distance from entry and if the player should go up or down to reach it
        Vec3 playerPos = mc.player.position();
        if (owner.getSettings().shouldShowDistance() && (shouldTakePriority() || owner.isBuiltIn()))
        {
            int distance = MathHelper.getDistance(owner.getPosition(), playerPos);

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
            String distanceFromObjective = MathHelper.getDistance(playerPos, owner.getPosition()) + suffix;
            int xOnCompass = compass.util.centerStringAroundX(distanceFromObjective, screenX);

            guiGraphicsExtractor.text(mc.font, distanceFromObjective, xOnCompass, compass.util.getYRowOnCompass(screenYRow + 1), renderColourForState, owner.getSettings().shouldShowShadow());
        }
    }

    public void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor)
    {
        guiGraphicsExtractor.verticalLine(screenX, compass.util.getYRowOnCompass(screenYRow) - 2, compass.util.getYRowOnCompass(screenYRow + 0.5), renderColourForState);
    }
}
