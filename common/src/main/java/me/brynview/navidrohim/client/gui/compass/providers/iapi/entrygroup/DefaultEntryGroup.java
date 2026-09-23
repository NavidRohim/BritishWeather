package me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.TickableAndCanExpire;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultEntryGroup implements TickableAndCanExpire
{
    private final List<DefaultEntry> entries = new ArrayList<>();

    public List<DefaultEntry> getEntries()
    {
        return entries;
    }

    public void addEntry(DefaultEntry entry)
    {
        entries.add(entry);
    }

    public void removeEntry(DefaultEntry entry)
    {
        entries.remove(entry);
    }

    public void clearEntries()
    {
        entries.clear();
    }

    @Override
    public void endTick(@NotNull LocalPlayer player)
    {
        getEntries().forEach(entry -> entry.endTick(player));
    }
}
