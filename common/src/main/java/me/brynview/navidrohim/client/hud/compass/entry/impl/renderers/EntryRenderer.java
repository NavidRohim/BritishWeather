package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.client.hud.compass.Compass.ENTRIES_ALLOWED_ON_SIDE;

// Might ditch
public class EntryRenderer<T extends AbstractEntry>
{
    private final static int MINIMUM_MARKER_LENGTH = 4;

    public enum RenderState
    {
        NONE,
        NORMAL,
        HIGHLIGHT,
        FOCUSED
    }

    protected int screenX;
    protected int screenYRow;
    private float distanceFromHeading;
    public float angleFromHeading;

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

        angleFromHeading = (float) MathHelper.angleFromPos(owner.getPosition(), mc.player.position());
        screenX = compass.util.getAngleX(angleFromHeading, owner.getSettings().shouldDisappearWhenOOB());
        screenYRow = 0;

        boolean isHighlightable = compass.util.isXHighlightable(screenX) && !owner.isBuiltIn();

        owner.isOnLeftEdge = screenX == compass.leftX;
        owner.isOnRightEdge = screenX == compass.rightX;
        distanceFromHeading = Math.abs(Mth.wrapDegrees(compass.yawInt - angleFromHeading));

        if (isHighlightable)
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
        return getRenderState() == RenderState.FOCUSED || owner.isBuiltIn() ? owner.getHighlightColor() : owner.defaultColour;
    }

    public RenderState getRenderState()
    {
        return renderState;
    }

    public void setRenderState(RenderState renderState)
    {
        this.renderState = renderState;
    }

    public float getDistanceFromHeading()
    {
        return distanceFromHeading;
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

        drawRawEntry(guiGraphicsExtractor, getRenderColour());
    }

    protected static @NonNull String getDisplayableMarker(AbstractEntry entryRenderable, boolean smart, int compassX)
    {
        // Draw entry marker
        Compass compass = Compass.getInstance();
        String displayableMarker;
        String marker = entryRenderable.getMarker();
        int markerLength = marker.length();

        if (entryRenderable.getRenderState() == RenderState.FOCUSED || markerLength <= MINIMUM_MARKER_LENGTH)
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
            int showLen = (int) Math.min(MINIMUM_MARKER_LENGTH + (perCharAmt * distFromCenter), markerLength);

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

    public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor, int renderColour)
    {
        drawMarker(guiGraphicsExtractor, renderColour);
        if (getRenderState() == RenderState.FOCUSED)
        {
            drawDecoration(guiGraphicsExtractor, renderColour);
        }
        drawDistance(guiGraphicsExtractor, renderColour);
    }

    protected void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        String displayableMarker = getDisplayableMarker(owner, owner.getMarker().length() <= 15, screenX);

        int markerWidth = mc.font.width(displayableMarker);
        int renderX = compass.util.centerWidthAroundX(markerWidth, screenX);
        int renderY = compass.util.getYRowOnCompass(screenYRow - 1) - 1;

        drawBackground(guiGraphicsExtractor, colour, renderX, renderY, markerWidth);
        guiGraphicsExtractor.text(mc.font, displayableMarker, renderX, renderY, colour, owner.getSettings().shouldShowShadow());
    }

    protected void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int colour, int x, int y, int markerWidth)
    {
        if (renderState == RenderState.FOCUSED && BritishWeather.getConfig().shouldShowPinBackground() && owner.getSettings().shouldShowBackground())
        {
            guiGraphicsExtractor.fill(x - 2, y - 2, x + markerWidth + 2, y + mc.font.lineHeight, owner.highlightBgColour);
            guiGraphicsExtractor.outline(x - 3, y - 3, markerWidth + 5, mc.font.lineHeight + 4, colour);
        }
    }

    protected void drawDistance(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        // If in view, render the distance from entry and if the player should go up or down to reach it
        Vec3 playerPos = mc.player.position();
        if (owner.getSettings().shouldShowDistance() && (owner.getRenderState() == RenderState.FOCUSED || owner.isBuiltIn()))
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

            guiGraphicsExtractor.text(mc.font, distanceFromObjective, xOnCompass, compass.util.getYRowOnCompass(screenYRow + 1), colour, owner.getSettings().shouldShowShadow());
        }
    }

    protected void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        guiGraphicsExtractor.verticalLine(screenX, compass.util.getYRowOnCompass(screenYRow) - 2, compass.util.getYRowOnCompass(screenYRow + 0.5), colour);
    }
}
