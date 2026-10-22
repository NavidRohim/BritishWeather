package me.brynview.navidrohim.client.hud.compass.entry.iapi.entrygroup;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.TickableAndExpirable;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
An entry group is just a list of entries. Each entry is normal and can be a subclass of DefaultEntry.
But, the entries will only have the end tick and not any other tick in the TickableAndExpirable interface.
Instead, the EntryGroup will tick which can dictate behaviour for the child entries.
 */
public class DefaultEntryGroup implements TickableAndExpirable
{
    private String level;

    public DefaultEntryGroup(ResourceKey<Level> level)
    {
        this.level = level.identifier().toString();
    }

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

    @Override
    public String getLevel()
    {
        return level;
    }
}
