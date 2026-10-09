package me.brynview.navidrohim.client.hud.compass.entry.iapi.entry;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.Singleton;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.TickableAndExpirable;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/*
 * DefaultEntry should be extended but is not abstract as it does have functionality by itself.
 * An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
 */
public class DefaultEntry implements TickableAndExpirable, EntryRenderable
{
    private final static String THINGY = "|";
    private final static int MINIMUM_MARKER_LENGTH = 4;

    private final Vec3 position; // Position of the entry in-game.
    private final String level; // What dimension the entry was made in.
    private final String entryId; // Unique entry ID. Not used for anything at the moment.

    private final Minecraft mc;

    private String marker; // What text will be rendered on the compass.
    private int markerWidthHalf; // The markers width halved.

    protected int colour;
    protected int highlightColour;

    private boolean isFocused = false;

    public DefaultEntry(Vec3 position, String level, String marker)
    {
        this(position, level, marker, ColorHelper.OBJECTIVE_MARKER_COLOUR);
    }

    public DefaultEntry(Vec3 position, String level, String marker, int colour)
    {
        this.mc = Minecraft.getInstance();
        this.level = level;
        this.position = position;

        if (!(this instanceof Singleton))
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = "%s.%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.position.x, (int) this.position.y, (int) this.position.z, level);
        } else
        {
            // If singleton, it is just the class name so it can be overridden.
            this.entryId = this.getClass().getSimpleName();
        }

        this.setColour(colour);
        this.setMarker(marker);
    }

    @Override
    public Vec3 getPosition()
    {
        return position;
    }

    public String getLevel()
    {
        return level;
    }

    @Override
    public String getMarker()
    {
        return marker;
    }

    @Override
    public int getMarkerHalfWidth()
    {
        return markerWidthHalf;
    }

    public void setMarker(String marker)
    {
        this.marker = marker;
        this.markerWidthHalf = mc.font.width(marker) / 2; // Calculate the markers width here, so it does not have to be calculated every render pass.
    }

    @Override
    public void setFocused(boolean focused)
    {
        isFocused = focused;
    }

    @Override
    public boolean isFocused()
    {
        return isFocused;
    }

    @Override
    public int getColour()
    {
        return colour;
    }

    public void setColour(int colour)
    {
        this.colour = colour;
        this.highlightColour = ColorHelper.shiftColourToOpacity(colour, 150);
    }

    @Override
    public int getHighlightColour()
    {
        return highlightColour;
    }

    public final void draw(GuiGraphicsExtractor guiGraphicsExtractor, @NotNull LocalPlayer player, boolean shouldStartTick, int entryX, int colour)
    {
        // Check if the entry should be rendered and the entry dimension matches the players current dimension
        if (!shouldRender() || !level.equals(player.level().dimension().identifier().toString()))
        {
            return;
        }

        if (shouldStartTick)
        {
            startTick(player);
        }

        Compass compass = Compass.getInstance();
        DefaultEntry.drawRawEntry(
                guiGraphicsExtractor,
                mc,
                player,
                compass,
                shouldShowDistance(),
                getDebugString(),
                false,
                this,
                entryX,
                colour
        );
    }

    public static void drawRawEntry(
            GuiGraphicsExtractor guiGraphicsExtractor,
            Minecraft mc,
            @NotNull LocalPlayer player,
            @NotNull Compass compass,
            boolean shouldShowDistance,
            @Nullable String debugString,
            boolean showAtCenter,
            EntryRenderable entryRenderable,
            int x,
            int colour
    )
    {
        if (compass.util.isXOutOfBounds(x))
        {
            return;
        }

        Vec3 playerPos = player.position();
        int compassX = showAtCenter ? compass.compassX + 50 : x;

        String displayableMarker = getDisplayableMarker(entryRenderable, entryRenderable.getMarker().length() <= 10, compass, compassX);
        int finalMarkerLen = mc.font.width(displayableMarker) / 2;

        FontHelper.draw(mc, guiGraphicsExtractor, displayableMarker, compassX - finalMarkerLen, compass.util.getYRowOnCompass(-1), colour, true, FontHelper.TextType.NONE);

        // If in view, render the distance from entry and if the player should go up or down to reach it
        if (shouldShowDistance && !compass.util.isXOutOfBounds(compassX))
        {
            int distance = MathHelper.getDistance(entryRenderable.getPosition(), playerPos);

            String suffix = "m";
            double heightDiff = playerPos.y - entryRenderable.getPosition().y;
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
            if (BritishWeather.getConfig().debug() && debugString != null)
            {
                FontHelper.draw(mc, guiGraphicsExtractor, debugString, compass.util.centerStringAroundX(debugString, compassX), compass.util.getYRowOnCompass(2), colour, FontHelper.TextType.NONE);
            }

            // Draw distance from entry
            String distanceFromObjective = MathHelper.getDistance(playerPos, entryRenderable.getPosition()) + suffix;
            int xOnCompass = compass.util.centerStringAroundX(distanceFromObjective, compassX);

            FontHelper.draw(mc, guiGraphicsExtractor, distanceFromObjective, xOnCompass, compass.util.getYRowOnCompass(1), colour, FontHelper.TextType.LABEL);
        }

        FontHelper.draw(mc, guiGraphicsExtractor, THINGY, compassX, compass.util.getYRowOnCompass(0), colour, FontHelper.TextType.LABEL);
    }

    private static @NonNull String getDisplayableMarker(EntryRenderable entryRenderable, boolean smart, Compass compass, int compassX)
    {
        // Draw entry marker
        String marker = entryRenderable.getMarker();
        int markerLength = marker.length();

        String displayableMarker;

        if (entryRenderable.isFocused() || markerLength <= MINIMUM_MARKER_LENGTH)
        {
            displayableMarker = marker;
        } else if (!smart) {
            displayableMarker = marker.substring(0, MINIMUM_MARKER_LENGTH) + "...";
        } else {

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
            } else {
                String baseMarker = marker.substring(markerLength - MINIMUM_MARKER_LENGTH, markerLength);
                String displayableRest = marker.substring((markerLength - showLen), markerLength - MINIMUM_MARKER_LENGTH);
                displayableMarker = "..." + displayableRest + baseMarker;
            }
        }
        return displayableMarker;
    }

    /*
    If the entry is persistent through game sessions.
    If true, the entry will be stored in an NBT file in the root .minecraft directory, and the filename will be the world folder name.
     */
    @Override
    public boolean isPersistent()
    {
        return false;
    }

    public boolean shouldRender()
    {
        return true;
    }

    public boolean shouldShowDistance()
    {
        return true;
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

    public static DefaultEntry of(Vec3 position, String level, String marker)
    {
        return new DefaultEntry(position, level, marker);
    }
}
