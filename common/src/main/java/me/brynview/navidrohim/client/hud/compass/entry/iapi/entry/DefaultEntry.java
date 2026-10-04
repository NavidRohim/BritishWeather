package me.brynview.navidrohim.client.hud.compass.entry.iapi.entry;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.Singleton;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
* DefaultEntry should be extended but is not abstract as it does have functionality by itself.
* An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
*/
public class DefaultEntry implements CompassEntry
{
    private final Vec3 position; // Position of the entry in-game.
    private final String level; // What dimension the entry was made in.
    private final String entryId; // Unique entry ID. Not used for anything at the moment.

    private final Minecraft mc;

    private String marker; // What text will be rendered on the compass.
    private int markerWidthHalf; // The markers width halved.

    protected int colour;

    public DefaultEntry(Vec3 position, String level, String marker)
    {
        this(position, level, marker, CompassEntry.OBJECTIVE_MARKER_COLOUR);
    }

    public DefaultEntry(Vec3 position, String level, String marker, int colour)
    {
        this.mc = Minecraft.getInstance();
        this.level = level;
        this.position = position;
        this.colour = colour;

        if (!(this instanceof Singleton))
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = "%s.%s.%s.%s.%s".formatted(this.getClass().getSimpleName(), (int) this.position.x, (int) this.position.y, (int) this.position.z, level);
        } else {
            // If singleton, it is just the class name so it can be overridden.
            this.entryId = this.getClass().getSimpleName();
        }

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
        this.markerWidthHalf = mc.font.width(marker) / 2; // Calculate the markers width here, so it does not have to be calculated every render pass.
    }

    @Override
    public int getColour()
    {
        return colour;
    }

    public final void draw(GuiGraphicsExtractor guiGraphicsExtractor, @NotNull LocalPlayer player, boolean shouldStartTick)
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

        DefaultEntry.drawRawEntry(
                guiGraphicsExtractor,
                mc,
                player,
                position,
                marker,
                getColour(),
                markerWidthHalf,
                shouldShowDistance(),
                getDebugString(),
                null
        );
    }

    public static void drawRawEntry(
            GuiGraphicsExtractor guiGraphicsExtractor,
            Minecraft mc,
            @NotNull LocalPlayer player,
            @NotNull Vec3 position,
            String marker,
            int colour,
            int markerWidthHalf,
            boolean shouldShowDistance,
            String debugString,
            @Nullable Integer posOnCompass
    )
    {
        Compass compass = Compass.getInstance();

        Vec3 playerPos = player.position();

        int compassX;
        if (posOnCompass == null)
        {
            double angleFromPosition = MathHelper.angleFromPos(position, playerPos);
            compassX = compass.util.getCompassScreenX((float) angleFromPosition, false);
        } else {
            compassX = compass.compassX + posOnCompass;
        }

        // Draw entry marker
        //FontHelper.drawEntry(mc, guiGraphicsExtractor, entry.getMarker(), entry.centerRelativeTo(compassX), this.yTextMiddle, entry.getColour(), FontHelper.TextType.NONE);
        FontHelper.draw(mc, guiGraphicsExtractor, marker, compassX - markerWidthHalf, compass.yTextMiddle, colour, true, FontHelper.TextType.NONE);

        // If in view, render the distance from entry and if the player should go up or down to reach it
        if (shouldShowDistance && !compass.util.isXOutOfBounds(compassX))
        {
            int distance = MathHelper.getDistance(position, playerPos);

            String suffix = "m ";
            double heightDiff = playerPos.y - position.y;
            // Both indicators check if the player is within 200 blocks, if not, just ignore.
            if (distance <= 200 && heightDiff >= 3) // down
            {
                suffix += "↓";
            } else if (distance <= 200 && heightDiff <= -3) // up
            {
                suffix += "↑";
            }

            // Show time remaining on timed entry if debug is enabled. I used this during persistence testing
            if (BritishWeather.getConfig().debug())
            {
                FontHelper.draw(mc, guiGraphicsExtractor, debugString, compass.util.getCenteredXForString(debugString, compassX), compass.yTextMiddle - 10, colour, FontHelper.TextType.NONE);
            }

            // Draw distance from entry
            String distanceFromObjective = MathHelper.getDistance(playerPos, position) + suffix;
            FontHelper.draw(mc, guiGraphicsExtractor, distanceFromObjective, compass.util.getCenteredXForString(distanceFromObjective, compassX) + 2, compass.util.getTextLocationY(), colour, FontHelper.TextType.LABEL);
        }
    }
    /*
    If the entry is persistent through game sessions.
    If true, the entry will be stored in an NBT file in the root .minecraft directory, and the filename will be the world folder name.
     */
    public boolean isPersistent()
    {
        return false;
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
