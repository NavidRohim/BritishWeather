package me.brynview.navidrohim.client.hud.compass.entry.entry;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.TickableAndExpirable;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.client.hud.compass.Compass.ENTRIES_ALLOWED_ON_SIDE;

/*
 * DefaultEntry should be extended but is not abstract as it does have functionality by itself.
 * An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
 */
public abstract class DefaultEntry implements TickableAndExpirable
{
    // Might ditch
    public class EntryRenderer
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

        protected RenderState renderState;
        protected final DefaultEntry owner;

        public EntryRenderer()
        {
            this.owner = DefaultEntry.this;
        }

        public void calculateState(Compass compass)
        {
            RenderState state = RenderState.NORMAL;

            float angle = (float) MathHelper.angleFromPos(getPosition(), mc.player.position());
            screenX = compass.util.getAngleX(angle, shouldDisappearWhenNotInView());
            screenYRow = 0;

            boolean isHighlightable = compass.util.isXHighlightable(screenX) && !owner.isBuiltIn();

            isOnLeftEdge = screenX == compass.leftX;
            isOnRightEdge = screenX == compass.rightX;
            distanceFromHeading = Math.abs(Mth.wrapDegrees(compass.yawInt - angle));

            if (isHighlightable)
            {
                state = RenderState.HIGHLIGHT;
            } else if (screenX == Integer.MAX_VALUE || (isOnLeftEdge && compass.entriesOnLeftEdge >= ENTRIES_ALLOWED_ON_SIDE) || (isOnRightEdge && compass.entriesOnRightEdge >= ENTRIES_ALLOWED_ON_SIDE) )
            {
                this.setRenderState(RenderState.NONE);
                return;
            }

            this.setRenderState(state);

            if (isOnLeftEdge)
            {
                compass.entriesOnLeftEdge++;
            }

            if (isOnRightEdge)
            {
                compass.entriesOnRightEdge++;
            }
        }

        public int getRenderColour()
        {
            return getRenderState() == RenderState.FOCUSED || isBuiltIn() ? owner.highlightColor : owner.defaultColour;
        }

        protected int getBackgroundColour(int colour)
        {
            int invertedRenderColour = colour ^ 0x00FFFFFF;
            return ARGB.greyscale(invertedRenderColour);
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
            if (!owner.shouldRender() || getRenderState() == RenderState.NONE)
            {
                return;
            }

            if (calculate)
            {
                calculateState(compass);
            }

            if (shouldStartTick)
            {
                startTick(player);
            }

            if (compass.util.isXOutOfBounds(screenX))
            {
                return;
            }

            drawRawEntry(guiGraphicsExtractor, getRenderColour());
        }

        protected static @NonNull String getDisplayableMarker(DefaultEntry entryRenderable, boolean smart, int compassX)
        {
            // Draw entry marker
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
            drawDistanceAndDebug(guiGraphicsExtractor, renderColour);
        }

        protected void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
        {
            String displayableMarker = getDisplayableMarker(owner, owner.getMarker().length() <= 15, screenX);

            int markerWidth = mc.font.width(displayableMarker);
            int renderX = compass.util.centerWidthAroundX(markerWidth, screenX);
            int renderY = compass.util.getYRowOnCompass(screenYRow - 1) - 1;

            boolean shadow = true;
            if (renderState == RenderState.FOCUSED && BritishWeather.getConfig().shouldShowPinBackground())
            {
                shadow = false;
                guiGraphicsExtractor.fill(renderX - 2, renderY - 2, renderX + markerWidth + 2, renderY + mc.font.lineHeight, owner.highlightBgColour);
                guiGraphicsExtractor.outline(renderX - 3, renderY - 3, markerWidth + 5, mc.font.lineHeight + 4, colour);
            }

            guiGraphicsExtractor.text(mc.font, displayableMarker, renderX, renderY, colour, shadow);
        }

        protected void drawDistanceAndDebug(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
        {
            // If in view, render the distance from entry and if the player should go up or down to reach it
            Vec3 playerPos = mc.player.position();
            if (owner.shouldShowDistance() && (owner.getRenderState() == RenderState.FOCUSED || owner.isBuiltIn()))
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

                // Show time remaining on timed entry if debug is enabled. I used this during persistence testing
                String debugString = getDebugString();
                if (BritishWeather.getConfig().debug() && debugString != null)
                {
                    guiGraphicsExtractor.text(mc.font, debugString, compass.util.centerStringAroundX(debugString, screenX), compass.util.getYRowOnCompass(screenYRow + 2), colour);
                    guiGraphicsExtractor.verticalLine(compass.util.centerWidthAroundX(2, screenX), compass.util.getYRowOnCompass(-1), compass.util.getYRowOnCompass(screenYRow + 3), colour);
                }

                // Draw distance from entry
                String distanceFromObjective = MathHelper.getDistance(playerPos, owner.getPosition()) + suffix;
                int xOnCompass = compass.util.centerStringAroundX(distanceFromObjective, screenX);

                guiGraphicsExtractor.text(mc.font, distanceFromObjective, xOnCompass, compass.util.getYRowOnCompass(screenYRow + 1), colour);
            }
        }

        protected void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
        {
            guiGraphicsExtractor.verticalLine(screenX, compass.util.getYRowOnCompass(screenYRow) - 2, compass.util.getYRowOnCompass(screenYRow + 0.5), colour);
            //guiGraphicsExtractor.text(mc.font, LUBBER, screenX, compass.util.getYRowOnCompass(screenYRow), colour, false);
        }
    }

    protected Vec3 position; // Position of the entry in-game.
    private final String level; // What dimension the entry was made in.
    private final String entryId; // Unique entry ID. Not used for anything at the moment.

    protected final Minecraft mc = Minecraft.getInstance();
    protected static final Compass compass = Compass.getInstance();
    protected EntryRenderer renderer;

    private String marker; // What text will be rendered on the compass.

    protected int highlightBgColour;
    protected int highlightColor;
    protected int defaultColour;

    public boolean isOnLeftEdge = false;
    public boolean isOnRightEdge = false;

    public DefaultEntry(Vec3 position, String level, String marker, int colour)
    {
        this.renderer = new EntryRenderer();
        this.level = level;
        this.position = position;

        if (this.isSingleton())
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = this.getClass().getSimpleName();
        } else
        {
            this.entryId = "%s.%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.getPosition().x, (int) this.getPosition().y, (int) this.getPosition().z, level);
            // If singleton, it is just the class name so it can be overridden.
        }

        this.setHighlightColor(colour);
        this.setMarker(marker);
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public String getLevel()
    {
        return level;
    }

    public String getMarker()
    {
        return marker;
    }

    public void setMarker(String marker)
    {
        this.marker = marker;
    }

    public boolean shouldRender()
    {
        return true;
    }

    public EntryRenderer getRenderer()
    {
        return renderer;
    }

    public int getHighlightColor()
    {
        return highlightColor;
    }

    public void setHighlightColor(int highlightColor)
    {
        this.highlightColor = highlightColor;
        this.highlightBgColour = getRenderer().getBackgroundColour(highlightColor);
        this.defaultColour = ColorHelper.shiftColourToOpacity(highlightColor, 124);
    }

    /*
    If the entry is persistent through game sessions.
    If true, the entry will be stored in an NBT file in the root .minecraft directory, and the filename will be the world folder name.
     */
    public boolean isPersistent()
    {
        return false;
    }

    public boolean isSingleton()
    {
        return false;
    }

    public boolean shouldShowDistance()
    {
        return true;
    }

    public boolean shouldDisappearWhenNotInView()
    {
        return true;
    }

    public EntryRenderer.RenderState getRenderState()
    {
        return this.getRenderer().renderState;
    }

    public final String getId()
    {
        return this.entryId;
    }

    /*
        This method exists so other custom data can be persistent when saved to NBT file.
        Anything can be written here.
         */
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
    }

    public String getDebugString()
    {
        return "P=%s".formatted(this.isPersistent());
    }

    @Override
    public String toString()
    {
        return getMarker();
    }
}
