package me.brynview.navidrohim.client.hud.compass.entry.entry;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.TickableAndExpirable;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;

/*
 * DefaultEntry should be extended but is not abstract as it does have functionality by itself.
 * An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
 */
public abstract class AbstractEntry implements TickableAndExpirable
{
    protected String marker; // What text will be rendered on the compass.
    protected Vec3 position; // Position of the entry in-game.
    protected final String level; // What dimension the entry was made in.
    protected final String entryId; // Unique entry ID. Not used for anything at the moment.

    protected static final Compass compass = Compass.getInstance();
    protected EntryRenderer<?> renderer;
    protected EntrySettings settings;

    public int highlightColor;
    public int highlightBgColour;
    public int defaultColour;

    public AbstractEntry(Vec3 position, String level, String marker, int colour, EntrySettings settings)
    {
        this.settings = settings;
        this.level = level;
        this.position = position;

        if (this.isSingleton())
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = "%s.".formatted(getType());
        } else
        {
            this.entryId = "%s.%s.%s.%s.%s".formatted(getType(), (int) this.getPosition().x, (int) this.getPosition().y, (int) this.getPosition().z, level);
            // If singleton, it is just the class name so it can be overridden.
        }

        this.setHighlightColor(colour);
        this.setMarker(marker);
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public Vec3 getPosition(float partialTicks)
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

    public EntryRenderer<?> getRenderer()
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
        this.highlightBgColour = ColorHelper.getBackgroundColour(highlightColor);
        this.defaultColour = ColorHelper.shiftColourToOpacity(highlightColor, 124);
    }

    public boolean isSingleton()
    {
        return false;
    }

    public final String getId()
    {
        return this.entryId;
    }

    public EntrySettings getSettings()
    {
        return settings;
    }

    @Override
    public String toString()
    {
        return getId();
    }

    public String getExtraInformation()
    {
        return "";
    }

    public String debugString()
    {
        return "DEBUG: isTracked=%s, id=%s, isSingleton=%s, isPersistent=%s".formatted(isTracked(), getId(), isSingleton(), getSettings().isPersistent());
    }

    public boolean isTracked()
    {
        return false;
    }

    public void track(Vec3 position)
    {
        throw new NotImplementedException();
    }

    public void untrack()
    {
        throw new NotImplementedException();
    }
}
