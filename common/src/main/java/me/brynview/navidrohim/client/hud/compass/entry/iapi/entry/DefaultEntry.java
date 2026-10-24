package me.brynview.navidrohim.client.hud.compass.entry.iapi.entry;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.Pin;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.Singleton;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.TickableAndExpirable;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.client.hud.compass.Compass.ENTRIES_ALLOWED_ON_SIDE;

/*
 * DefaultEntry should be extended but is not abstract as it does have functionality by itself.
 * An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
 */
public class DefaultEntry implements TickableAndExpirable
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

            float angle = (float) MathHelper.angleFromPos(position, mc.player.position());
            boolean shouldDisappear = owner instanceof Pin entry && entry.shouldDisappearWhenNotInView();

            screenX = (int) compass.util.getAngleX(angle, shouldDisappear);
            screenYRow = 0;

            boolean isHighlightable = compass.util.isXHighlightable(screenX) && !owner.isBuiltIn();

            isOnLeftEdge = screenX == compass.farLeftX;
            isOnRightEdge = screenX == compass.farRightX;
            distanceFromHeading = Math.abs(Mth.wrapDegrees(compass.yawInt - angle));

            if (isHighlightable)
            {
                state = RenderState.HIGHLIGHT;
            } else if ( (isOnLeftEdge && compass.entriesOnLeftEdge >= ENTRIES_ALLOWED_ON_SIDE) || (isOnRightEdge && compass.entriesOnRightEdge >= ENTRIES_ALLOWED_ON_SIDE) )
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
            return getRenderState() == RenderState.FOCUSED || isBuiltIn() ? owner.highlightColor : owner.color;
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
            if (calculate)
            {
                calculateState(compass);
            }

            // Check if the entry should be rendered and the entry dimension matches the players current dimension
            if (!owner.shouldRender() || getRenderState() == RenderState.NONE)
            {
                return;
            }

            if (shouldStartTick)
            {
                startTick(player);
            }

            if (compass.util.isXOutOfBounds(screenX))
            {
                return;
            }

            drawRawEntry(
                    guiGraphicsExtractor,
                    mc,
                    player,
                    compass
            );
        }

        private static @NonNull String getDisplayableMarker(DefaultEntry entryRenderable, boolean smart, Compass compass, int compassX)
        {
            // Draw entry marker
            String marker = entryRenderable.getMarker();
            int markerLength = marker.length();

            String displayableMarker;

            if (entryRenderable.getRenderState() == RenderState.FOCUSED || markerLength <= MINIMUM_MARKER_LENGTH)
            {
                displayableMarker = marker;
            } else if (!smart)
            {
                displayableMarker = marker.substring(0, MINIMUM_MARKER_LENGTH) + "...";
            } else
            {

                int safeX = Math.min(compassX, compass.compassX);
                int compassHalf = compass.compassScaledWidthHalf;

                float markerLengthWithoutBeginning = markerLength - MINIMUM_MARKER_LENGTH;
                int distFromCenter = compassHalf - Math.abs(compass.compassX - safeX);
                float perCharAmt = markerLengthWithoutBeginning / compassHalf;
                int showLen = (int) Math.min(MINIMUM_MARKER_LENGTH + (perCharAmt * distFromCenter), markerLength);

                if (compassX < compass.compassX) // left side
                {
                    String baseMarker = marker.substring(0, MINIMUM_MARKER_LENGTH);
                    String displayableRest = marker.substring(MINIMUM_MARKER_LENGTH, showLen);
                    displayableMarker = baseMarker + displayableRest + "...";
                } else
                {
                    String baseMarker = marker.substring(markerLength - MINIMUM_MARKER_LENGTH, markerLength);
                    String displayableRest = marker.substring((markerLength - showLen), markerLength - MINIMUM_MARKER_LENGTH);
                    displayableMarker = "..." + displayableRest + baseMarker;
                }
            }
            return displayableMarker;
        }

        public void drawRawEntry(
                GuiGraphicsExtractor guiGraphicsExtractor,
                Minecraft mc,
                @NotNull LocalPlayer player,
                @NotNull Compass compass
        )
        {
            drawMarker(guiGraphicsExtractor);
            drawDistanceAndDebug(guiGraphicsExtractor);
            drawDecoration(guiGraphicsExtractor);
        }

        protected void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor)
        {
            String displayableMarker = getDisplayableMarker(owner, owner.getMarker().length() <= 15, compass, screenX);
            FontHelper.draw(mc, guiGraphicsExtractor, displayableMarker, screenX - mc.font.width(displayableMarker) / 2, compass.util.getYRowOnCompass(screenYRow - 1), getRenderColour(), true, FontHelper.TextType.NONE);
        }

        protected void drawDistanceAndDebug(GuiGraphicsExtractor guiGraphicsExtractor)
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
                    FontHelper.draw(mc, guiGraphicsExtractor, debugString, compass.util.centerStringAroundX(debugString, screenX), compass.util.getYRowOnCompass(screenYRow + 2), getRenderColour(), FontHelper.TextType.NONE);
                }

                // Draw distance from entry
                String distanceFromObjective = MathHelper.getDistance(playerPos, owner.getPosition()) + suffix;
                int xOnCompass = compass.util.centerStringAroundX(distanceFromObjective, screenX);

                FontHelper.draw(mc, guiGraphicsExtractor, distanceFromObjective, xOnCompass, compass.util.getYRowOnCompass(screenYRow + 1), getRenderColour(), FontHelper.TextType.LABEL);
            }
        }

        protected void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor)
        {
            FontHelper.draw(mc, guiGraphicsExtractor, THINGY, screenX, compass.util.getYRowOnCompass(screenYRow), getRenderColour(), FontHelper.TextType.LABEL);
        }
    }

    public final static String THINGY = "|";

    private Vec3 position; // Position of the entry in-game.
    private final String level; // What dimension the entry was made in.
    private final String entryId; // Unique entry ID. Not used for anything at the moment.

    protected final Minecraft mc;
    protected final Compass compass = Compass.getInstance();
    protected EntryRenderer renderer;

    private String marker; // What text will be rendered on the compass.

    protected int highlightColor;
    protected int color;

    public boolean isOnLeftEdge = false;
    public boolean isOnRightEdge = false;


    public DefaultEntry(Vec3 position, String level, String marker)
    {
        this(position, level, marker, ColorHelper.OBJECTIVE_MARKER_COLOUR);
    }

    public DefaultEntry(Vec3 position, String level, String marker, int colour)
    {
        this.renderer = new EntryRenderer();
        this.mc = Minecraft.getInstance();
        this.level = level;
        this.position = position;

        if (this instanceof Singleton)
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = this.getClass().getSimpleName();
        } else
        {
            this.entryId = "%s.%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.position.x, (int) this.position.y, (int) this.position.z, level);
            // If singleton, it is just the class name so it can be overridden.
        }

        this.setHighlightColor(colour);
        this.setMarker(marker);
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public void setPosition(Vec3 position)
    {
        if (this.isPersistent())
        {
            throw new UnsupportedOperationException("Persistent entries cannot have their position modified.");
        }

        this.position = position;
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

    protected void setRenderer(EntryRenderer renderer)
    {
        this.renderer = renderer;
    }

    public int getHighlightColor()
    {
        return highlightColor;
    }

    public void setHighlightColor(int highlightColor)
    {
        this.highlightColor = highlightColor;
        this.color = ColorHelper.shiftColourToOpacity(highlightColor, 124);
    }

    /*
    If the entry is persistent through game sessions.
    If true, the entry will be stored in an NBT file in the root .minecraft directory, and the filename will be the world folder name.
     */
    public boolean isPersistent()
    {
        return false;
    }

    public boolean shouldShowDistance()
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

    public static DefaultEntry of(Vec3 position, String level, String marker)
    {
        return new DefaultEntry(position, level, marker);
    }
}
