package me.brynview.navidrohim.client.hud.compass.entry.entry;

import java.util.function.Consumer;

// Very useless at the moment, but I hope in the future to add animations for notifications (Maybe, when you reach a death point it can increase scale-flash for a bit)
public interface EntryAnimation
{
    AbstractEntry getEntryOwner();
    Consumer<Double> lerpChangeCallback();
    boolean hasFinished();

    default void reset() {}
    default void interrupted() {}
    default void tick() {}
}
