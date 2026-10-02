package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import net.minecraft.network.FriendlyByteBuf;
/*
TimeMethod basically counts time for a TimedEntry and tells it when to expire.
The entry doesn't just count itself as some entries might count time past while offline (RealTime class) or only count time past in-game (GameTime).
To simplify this, the TimeMethod interface was made if in the future I add other ways of counting. Which sounds kinda stupid.
 */
public interface TimeMethod
{
    default void tick() {}

    boolean isExpired();

    default long timeLeft()
    {
        return 0L;
    }

    /*
    Encode the TimeMethod so it persists. Usually encodes the current time, and what time it will end.
     */
    default void encode(FriendlyByteBuf friendlyByteBuf)
    {
    }
}
