package me.brynview.navidrohim.client.gui.compass.providers.iapi;

import net.minecraft.network.FriendlyByteBuf;

public interface TimeMethod
{
    default void tick() {};

    boolean isExpired();

    default long timeLeft()
    {
        return 0L;
    };

    default void encode(FriendlyByteBuf friendlyByteBuf)
    {
    }
}
