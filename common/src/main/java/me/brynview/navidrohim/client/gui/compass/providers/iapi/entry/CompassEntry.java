package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.TickableAndExpirable;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.world.phys.Vec3;

// Maybe get rid of this interface. Is useless.
public interface CompassEntry extends TickableAndExpirable
{
    int OBJECTIVE_MARKER_COLOUR = ColorHelper.rgb(243, 238, 159, 255);

    Vec3 getPosition();

    String getMarker();

    void setMarker(String marker);

    default boolean shouldRender()
    {
        return true;
    }

    default boolean shouldShowDistance()
    {
        return true;
    }

    default int getColour()
    {
        return OBJECTIVE_MARKER_COLOUR;
    }
}
