package me.brynview.navidrohim.client.hud.compass.entry.entry;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.TickableAndExpirable;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/*
 * DefaultEntry should be extended but is not abstract as it does have functionality by itself.
 * An "entry" in HudCompass context is something that can be rendered on the compass. Perhaps a pin, objective or waypoint.
 */
public abstract class DefaultEntry implements TickableAndExpirable
{

    protected Vec3 position; // Position of the entry in-game.
    private final String level; // What dimension the entry was made in.
    private final String entryId; // Unique entry ID. Not used for anything at the moment.

    protected static final Compass compass = Compass.getInstance();
    protected EntryRenderer renderer;

    private String marker; // What text will be rendered on the compass.

    public int highlightBgColour;
    protected int highlightColor;
    public int defaultColour;

    public boolean isOnLeftEdge = false;
    public boolean isOnRightEdge = false;

    public DefaultEntry(Vec3 position, String level, String marker, int colour)
    {
        this.renderer = new EntryRenderer(this);
        this.level = level;
        this.position = position;

        if (this.isSingleton())
        {
            // Non-singleton ID is made up of the entries class, then xyz position, then dimension.
            this.entryId = this.getClass().getSimpleName();
        } else
        {
            this.entryId = "%s.%s.%s.%s".formatted((int) this.getPosition().x, (int) this.getPosition().y, (int) this.getPosition().z, level);
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
        return this.getRenderer().getRenderState();
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
