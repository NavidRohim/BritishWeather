package me.brynview.navidrohim.client.hud.compass.entry.builtin.entrygroup;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/*
Entry group for each map in a players inventory.

For each explorer / abandoned camp map, an entry will be added to the compass so the player can go there
without having to hold the map out.
 */
public class MapEntryGroup extends DefaultEntryGroup<MapEntryGroup.MapEntry>
{
    private static final String MARKER = "⛺";
    private final TextureAtlas atlas;

    public MapEntryGroup(ResourceKey<Level> level)
    {
        super(level);

        // This constructor is called inside static init block, so I thought getInstance() or getAtlasManager() would fail because they are called too early.
        // But this class must be loaded later or something?
        this.atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.MAP_DECORATIONS);
    }

    public final class MapEntry extends DefaultEntry
    {
        private final class MapEntryRenderer extends EntryRenderer
        {
            @Override
            protected void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor)
            {
                TextureAtlasSprite sprite = atlas.getSprite(decoId);
                guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, screenX - 4, compass.util.getYRowOnCompass(screenYRow) - 1, 8, 8);
            }

            @Override
            public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor)
            {
                drawMarker(guiGraphicsExtractor);
                drawDistanceAndDebug(guiGraphicsExtractor);
            }

            @Override
            public int getRenderColour()
            {
                return ColorHelper.WHITE;
            }
        }

        private final Identifier decoId;

        public MapEntry(Vec3 position, Identifier mapDeco)
        {
            super(position, Level.OVERWORLD.identifier().toString(), MARKER);
            this.decoId = mapDeco;
            this.renderer = new MapEntryRenderer();
        }

        @Override
        public boolean isBuiltIn()
        {
            return true;
        }
    }

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

                    // Pure bodge dog shit I swear
                    MapItem.getSavedData(itemStack, player.level())
                            .getDecorations()
                            .forEach(d ->
                                    {
                                        if (saved.type().is(d.getSpriteLocation()))
                                        {
                                            this.addEntry(new MapEntry(pos, d.getSpriteLocation()));
                                        }
                                    }
                            );
                }
            } catch (NullPointerException _) {} // Will be thrown if the map doesn't have a decoration. Which will be the case if the map is just a normal map. Ignore.}
        }
    }

    /*
    Clear all map entries when the entry HUD disappears to prevent old maps from being rendered.
     */
    @Override
    public void endTick(@NotNull LocalPlayer player)
    {
        this.clearEntries();
    }

    @Override
    public boolean isBuiltIn()
    {
        return true;
    }
}
