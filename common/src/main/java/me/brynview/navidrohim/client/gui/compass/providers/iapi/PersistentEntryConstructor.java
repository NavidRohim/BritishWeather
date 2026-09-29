package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import net.minecraft.world.phys.Vec3;

public interface PersistentEntryConstructor
{
    DefaultEntry constructEntry(String marker, Vec3 pos);
}
