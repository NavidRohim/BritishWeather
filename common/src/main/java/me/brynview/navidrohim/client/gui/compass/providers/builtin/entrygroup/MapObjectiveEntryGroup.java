package me.brynview.navidrohim.client.gui.compass.providers.builtin.entrygroup;

import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup.DefaultEntryGroup;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/*
Entry group for each map in a players inventory.

For each explorer / abandoned camp map, an entry will be added to the compass so the player can go there
without having to hold the map out.
 */
public class MapObjectiveEntryGroup extends DefaultEntryGroup
{
    private static final String MARKER = "\uD83C\uDFF0";

    /*
    Only get the maps the player currently has at the start tick (When the player first activates the entry HUD)
     */
    @Override
    public void startTick(@NotNull LocalPlayer player)
    {
        Inventory inventory = player.getInventory();
        for (ItemStack itemStack : inventory)
        {
            try
            {
                if (itemStack.getItem() instanceof MapItem) // Check if MapItem. This is the case for all maps. Normal maps, abandoned camp maps or explorer maps
                {
                    // Get the "decoration" as minecraft calls it. Which is the abandoned structure or POI.
                    MapDecorations.Entry saved = itemStack.get(DataComponents.MAP_DECORATIONS).decorations().get("+"); // What is this key?
                    Vec3 pos = new Vec3(saved.x(), 0, saved.z());

                    this.addEntry(DefaultEntry.of(pos, player.level().dimension().identifier().toString(), MARKER));
                }
            } catch (NullPointerException _) // Will be thrown if the map doesn't have a decoration. Which will be the case if the map is just a normal map. Ignore.
            {}
        }
    }

    /*
    Clear all map entries when the entry HUD disappears to prevent old maps from being rendered.
     */
    @Override
    public void endTick(@NotNull LocalPlayer player)
    {
        super.endTick(player);
        this.clearEntries();
    }
}
