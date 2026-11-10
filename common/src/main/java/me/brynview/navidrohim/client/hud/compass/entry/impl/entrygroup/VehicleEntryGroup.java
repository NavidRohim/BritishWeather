package me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VehicleEntryGroup extends AbstractEntryGroup<VehicleEntryGroup.VehicleEntry>
{
    private static final int SPRITE_SIZE = 10;

    private final @NotNull TextureAtlas atlas;
    private String displayDepth = "";
    private BlockPos pos = BlockPos.ZERO;
    private @Nullable Entity currentlyRiddenVehicle = null;

    public VehicleEntryGroup(@Nullable ResourceKey<Level> level)
    {
        super(level);
        this.atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS);
    }

    public final class VehicleEntry extends AbstractEntry
    {
        public final class VehicleRenderer extends EntryRenderer<VehicleEntry>
        {
            public VehicleRenderer(VehicleEntry owner)
            {
                super(owner);
            }

            @Override
            public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context)
            {
                drawMarker(guiGraphicsExtractor, context, screenX, screenYRow);
                drawSpeed(guiGraphicsExtractor, context);
            }

            @Override
            public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context, int x, int yRow)
            {
                TextureAtlasSprite vehicleSprite = atlas.getSprite(texture);
                guiGraphicsExtractor.blitSprite(GuiPipeline.OPAQUE_TEXTURED_BACKGROUND, vehicleSprite, x - 5, context.getYOnRowCentered(yRow, SPRITE_SIZE), SPRITE_SIZE, SPRITE_SIZE);
            }

            private void drawSpeed(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext)
            {
                String displayableSpeed = compassRenderContext.calculateDisplayableSpeed(vehicle, false);
                if (!displayableSpeed.equals("0m/s"))
                {
                    guiGraphicsExtractor.text(mc.font, displayableSpeed, compassRenderContext.centerStringAroundX(displayableSpeed, screenX), compassRenderContext.getYRowOnCompass(screenYRow + 1), renderColour);
                }
            }
        }

        private static final EntrySettings settings = new EntrySettings()
                .setPersistent(false)
                .setShadow(false)
                .setShowDistance(false)
                .setShowBackground(false);

        private final VehicleEntity vehicle;
        private final Identifier texture;

        public VehicleEntry(VehicleEntity entity, Identifier texture)
        {
            this.vehicle = entity;
            super(entity.position(), entity.level().dimension().identifier().toString(), "?", ColorHelper.WHITE, settings);
            this.texture = texture.withPrefix("item/");
            this.renderer = new VehicleRenderer(this);
        }

        @Override
        public boolean isBuiltIn()
        {
            return true;
        }

        @Override
        public Vec3 getPosition()
        {
            return vehicle.position();
        }

    }

    @Override
    public void startTick(@NotNull LocalPlayer player)
    {
        currentlyRiddenVehicle = player.getVehicle();
        GeneralUtils.getEntitiesInView(player, VehicleEntity.class).forEach(entity ->
        {
            @Nullable ItemStack vehicleItemStack = entity.getPickResult();
            if (vehicleItemStack == null)
            {
                return;
            }
            @Nullable Identifier vehicleTexture = vehicleItemStack.get(DataComponents.ITEM_MODEL);
            if (vehicleTexture == null)
            {
                return;
            }

            this.addEntry(new VehicleEntry(entity, vehicleTexture));
        }
        );
    }

    @Override
    public void renderExtra(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context)
    {
        if (currentlyRiddenVehicle != null)
        {
            String displayString = currentlyRiddenVehicle instanceof AbstractHorse ? context.getDisplayableSpeed() : context.getDisplayableSpeedVertical();

            if (currentlyRiddenVehicle instanceof AbstractBoat boat)
            {
                if (!boat.blockPosition().equals(pos) || pos == BlockPos.ZERO)
                {
                    displayDepth = context.getDepthBelow(boat) + " depth";
                }

                displayString += " :: " + displayDepth;
                pos = boat.blockPosition();
            }

            guiGraphicsExtractor.text(context.font, displayString, context.rightContentX, context.getYRowOnCompass(0), ColorHelper.WHITE);
        }
    }

    @Override
    public void endTick(@NotNull LocalPlayer player)
    {
        this.clearEntries();
        currentlyRiddenVehicle = null;
        displayDepth = "";
        pos = BlockPos.ZERO;
    }

    @Override
    public boolean isBuiltIn()
    {
        return true;
    }
}
