package me.brynview.navidrohim.client.gui.compass.providers.iapi;

public interface TickableAndCanExpire
{
    default void tick() {}
    default void displayTick() {}
    default void startTick() {}
    default void endTick() {}

    default boolean hasExpired()
    {
        return false;
    }

}
