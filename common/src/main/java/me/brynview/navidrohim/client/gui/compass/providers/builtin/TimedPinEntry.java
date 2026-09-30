package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.providers.builtin.time.GameTime;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultTimedEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;

public class TimedPinEntry extends DefaultTimedEntry
{
    public TimedPinEntry(Vec3 position, String level, String marker, TimeUnit unit, int duration)
    {
        super(position, level, marker, new GameTime(unit, duration));
    }

    public TimedPinEntry(String s, Vec3 vec3, String level, FriendlyByteBuf friendlyByteBuf)
    {
        GameTime gameTime = GameTime.decode(friendlyByteBuf);
        super(vec3, level, s, gameTime);
    }

    @Override
    public boolean isPersistent()
    {
        return true;
    }
}
