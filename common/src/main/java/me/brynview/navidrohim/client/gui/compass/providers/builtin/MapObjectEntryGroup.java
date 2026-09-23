package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup.DefaultEntryGroup;

public class MapObjectEntryGroup extends DefaultEntryGroup
{
    @Override
    public void startTick()
    {

    }

    @Override
    public void endTick()
    {
        this.clearEntries();
    }
}
