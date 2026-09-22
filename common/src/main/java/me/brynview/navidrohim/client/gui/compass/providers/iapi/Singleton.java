package me.brynview.navidrohim.client.gui.compass.providers.iapi;

public interface Singleton
{
    default boolean isAbsolute()
    {
        return true;
    }
}
