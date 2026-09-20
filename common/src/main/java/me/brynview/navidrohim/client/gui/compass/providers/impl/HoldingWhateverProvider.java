package me.brynview.navidrohim.client.gui.compass.providers.impl;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.providers.CompassProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.worldgen.AbandonedCampStructurePools;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.fixes.ExplorerMapItemFix;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class HoldingWhateverProvider implements CompassProvider
{

    private Vec3 guideToPos = new Vec3(0,0,0);
    private boolean shouldShow = false;

    @Override
    public Identifier getImage()
    {
        return null;
    }

    @Override
    public String getText()
    {
        return "C";
    }

    @Override
    public Vec3 getPosition(@NotNull LocalPlayer player)
    {
        return guideToPos;
    }

    @Override
    public boolean shouldShow()
    {
        return shouldShow;
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        boolean didChange = false;
        Inventory inventory = player.getInventory();

        if (inventory.isEmpty())
        {
            return;
        }

        for (ItemStack itemStack : player.getInventory())
        {
            if (itemStack.is(Items.ABANDONED_CAMP_MAP))
            {
                MapDecorations.Entry saved = itemStack.get(DataComponents.MAP_DECORATIONS).decorations().get("+"); // What is this key man?
                guideToPos = new Vec3(saved.x(), 0, saved.z());
                didChange = true;
                shouldShow = true;
            }
        }

        if (!didChange)
        {
            shouldShow = false;
            guideToPos = new Vec3(0,0,0);
        }
    }
}
