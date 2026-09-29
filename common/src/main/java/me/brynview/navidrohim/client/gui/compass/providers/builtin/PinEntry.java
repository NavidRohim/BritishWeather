package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.HudCompass;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.PersistentEntriesManager;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import net.minecraft.world.phys.Vec3;

public class PinEntry extends DefaultEntry
{
    public PinEntry(Vec3 position)
    {
        super(position, "P");
    }

    public PinEntry(Vec3 position, String marker)
    {
        super(position, marker);
    }

    @Override
    public boolean isPersistent()
    {
        return true;
    }
}
