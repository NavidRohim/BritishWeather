package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class DefaultCompassProvider implements CompassProvider
{
    protected Map<Vec3, DefaultCompassProviderEntry> entries = new HashMap<>();

    @Override
    public Collection<DefaultCompassProviderEntry> getEntries()
    {
        return entries.values();
    }

    public void addEntry(DefaultCompassProviderEntry entry)
    {
        this.entries.put(entry.getPosition(),  entry);
    }

    public boolean isSingleton()
    {
        return false;
    }
}
