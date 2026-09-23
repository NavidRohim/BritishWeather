package me.brynview.navidrohim.client.gui.compass.providers.builtin;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup.DefaultEntryGroup;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MapObjectEntryGroup extends DefaultEntryGroup
{
    private static final String MARKER = "C";

    @Override
    public void startTick(@NotNull LocalPlayer player)
    {
        Inventory inventory = player.getInventory();
        if (inventory.isEmpty())
        {
            return;
        }

        for (ItemStack itemStack : player.getInventory())
        {
            try
            {
                if (itemStack.getItem() instanceof MapItem)
                {
                    MapDecorations.Entry saved = itemStack.get(DataComponents.MAP_DECORATIONS).decorations().get("+"); // What is this key?
                    Vec3 pos = new Vec3(saved.x(), 0, saved.z());

                    this.addEntry(DefaultEntry.of(pos, MARKER));
                }
            } catch (NullPointerException error)
            {
                Constants.debug("ItemStack is MapItem but MAP_DECORATIONS DataComponent does not have expected key!");
            }
        }
    }

    @Override
    public void endTick(@NotNull LocalPlayer player)
    {
        this.clearEntries();
    }
}
