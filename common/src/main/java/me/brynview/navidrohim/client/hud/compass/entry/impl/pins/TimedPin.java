package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/*
Same as PinEntry but timed.
 */
public class TimedPin extends PinEntry
{
    protected @NotNull TimeKeeper timeMethod;

    public TimedPin(Vec3 position, String level, String marker, @NonNull TimeKeeper timeMethod, boolean isPersistent, int colour, boolean shouldShowDistance, boolean shouldDisappearWhenNotInView)
    {
        super(position, level, marker, colour, isPersistent, shouldShowDistance, shouldDisappearWhenNotInView);
        this.timeMethod = timeMethod;
    }

    public TimedPin(String s, int colour, Vec3 vec3, String level, FriendlyByteBuf friendlyByteBuf)
    {
        super(s, colour, vec3, level, friendlyByteBuf);
        this.timeMethod = TimeKeeper.TimeMethods.getTimeKeeperFromBuffer(friendlyByteBuf);
    }

    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        super.serialise(friendlyByteBuf);
        timeMethod.encode(friendlyByteBuf);
    }

    @Override
    public boolean hasExpired()
    {
        return timeMethod.isExpired();
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        this.timeMethod.tick();
    }
}
