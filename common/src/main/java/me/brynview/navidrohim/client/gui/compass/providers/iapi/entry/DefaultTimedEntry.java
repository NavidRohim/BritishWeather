package me.brynview.navidrohim.client.gui.compass.providers.iapi.entry;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.TimeMethod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public abstract class DefaultTimedEntry extends DefaultEntry
{
    public @Nullable TimeMethod timeMethod;

    public DefaultTimedEntry(Vec3 position, String level, String marker, @Nullable TimeMethod timeMethod)
    {
        super(position, level, marker);
        this.timeMethod = timeMethod;
    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        this.timeMethod.encode(friendlyByteBuf);
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        timeMethod.tick();
    }

    @Override
    public boolean hasExpired()
    {
        return timeMethod != null && timeMethod.isExpired();
    }
}
