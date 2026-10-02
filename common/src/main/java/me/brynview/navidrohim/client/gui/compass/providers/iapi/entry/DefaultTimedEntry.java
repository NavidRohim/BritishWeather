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

/*
Normal entry but will expire after specified amount of time.

Constructor takes TimeMethod instance, which basically tells the entry when to expire and counts.
The entry doesn't just count itself as some entries might count time past while offline (RealTime class) or only count time past in-game (GameTime).
To simplify this, the TimeMethod interface was made if in the future I add other ways of counting. Which sounds kinda stupid.
 */
public abstract class DefaultTimedEntry extends DefaultEntry
{
    public @NotNull TimeMethod timeMethod;

    public DefaultTimedEntry(Vec3 position, String level, String marker, @NotNull TimeMethod timeMethod)
    {
        super(position, level, marker);
        this.timeMethod = timeMethod;
    }

    /*
    Serialise the TimeMethod, to make sure persistent entries work with timed entries.
     */
    @Override
    public void serialise(FriendlyByteBuf friendlyByteBuf)
    {
        this.timeMethod.encode(friendlyByteBuf);
    }

    /*
    Tick the TimeMethod. GameTime relies on this for counting in-game ticks but RealTime doesn't
     */
    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        timeMethod.tick();
    }

    /*
    Expire the entry when time has elapsed.
     */
    @Override
    public boolean hasExpired()
    {
        return timeMethod.isExpired();
    }
}
