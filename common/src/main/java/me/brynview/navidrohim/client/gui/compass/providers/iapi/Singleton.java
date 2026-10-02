package me.brynview.navidrohim.client.gui.compass.providers.iapi;

/*
Singleton interface which, if implemented, means an entry can only have one instance on the compass at a time.
 */
public interface Singleton
{
    /*
    If isAbsolute is true, if a current singleton entry exists of the same class, it will be overwritten by this one.
     */
    default boolean isAbsolute()
    {
        return true;
    }
}
