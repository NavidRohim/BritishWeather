package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PinEntry extends DefaultEntry
{
    public PinEntry(Vec3 position, String level)
    {
        super(position, level, "P");
    }

    public PinEntry(String s, Vec3 vec3, String level, FriendlyByteBuf extraData)
    {
        super(vec3, level, s);
    }

    @Override
    public boolean isPersistent()
    {
        return true;
    }
}
