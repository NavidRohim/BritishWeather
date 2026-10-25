package me.brynview.navidrohim.client.hud.compass.entry.builtin.entrygroup;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VehicleEntryGroup extends DefaultEntryGroup<VehicleEntryGroup.VehicleEntry>
{

    private static final String ROWBOAT = "\uD83D\uDEA3";
    private static final String TRAIN = "\uD83D\uDE86";

    private String displayDepth = "";
    private BlockPos pos = BlockPos.ZERO;
    private @Nullable Entity currentlyRiddenVehicle = null;

    public VehicleEntryGroup(@Nullable ResourceKey<Level> level)
    {
        super(level);
    }

    public static final class VehicleEntry extends DefaultEntry
    {
        public final class VehicleRenderer extends EntryRenderer
        {
            @Override
            public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, @NotNull LocalPlayer player, @NotNull Compass compass)
            {
                drawMarker(guiGraphicsExtractor);
                drawSpeed(guiGraphicsExtractor);
            }

            private void drawSpeed(GuiGraphicsExtractor guiGraphicsExtractor)
            {
                String displayableSpeed = getDisplayableSpeed(vehicle, false);
                if (!displayableSpeed.equals("0m/s"))
                {
                    FontHelper.draw(mc, guiGraphicsExtractor, displayableSpeed, compass.util.centerStringAroundX(displayableSpeed, screenX), compass.util.getYRowOnCompass(screenYRow + 1), getRenderColour(), FontHelper.TextType.VALUE);
                }
            }
        }

        private final VehicleEntity vehicle;

        public VehicleEntry(VehicleEntity entity, String marker)
        {
            this.vehicle = entity;
            super(entity.position(), entity.level().dimension().identifier().toString(), marker);
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

        @Override
        protected EntryRenderer initRenderer()
        {
            return new VehicleRenderer();
        }
    }

    @Override
    public void startTick(@NotNull LocalPlayer player)
    {
        currentlyRiddenVehicle = player.getVehicle();
        GeneralUtils.getEntitiesInView(player, VehicleEntity.class).forEach(entity -> {
            String marker = ROWBOAT;
            if (entity instanceof AbstractMinecart)
            {
                marker = TRAIN;
            }

            this.addEntry(new VehicleEntry(entity, marker));
        });
    }

    @Override
    public void renderExtra(GuiGraphicsExtractor guiGraphicsExtractor, Compass compass)
    {
        if (currentlyRiddenVehicle != null)
        {
            String rawSpeedString = "Speed: " + getDisplayableSpeed(currentlyRiddenVehicle, !(currentlyRiddenVehicle instanceof AbstractHorse));
            FontHelper.draw(compass.mc, guiGraphicsExtractor, rawSpeedString, compass.rightContentX, compass.util.getYRowOnCompass(0), ColorHelper.WHITE, FontHelper.TextType.VALUE);

            if (currentlyRiddenVehicle instanceof AbstractBoat boat)
            {
                if (!boat.blockPosition().equals(pos))
                {
                    int depth = ((int) boat.getY()) - findOceanFloor(boat);
                    displayDepth = "Depth: " + depth + "m";
                }

                FontHelper.draw(compass.mc, guiGraphicsExtractor, displayDepth, compass.rightContentX, compass.util.getYRowOnCompass(1), ColorHelper.WHITE,  FontHelper.TextType.VALUE);
                pos = boat.blockPosition();
            }
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

    public static int findOceanFloor(Entity entity) {
        BlockPos.MutableBlockPos entityBlockPosCopy = entity.blockPosition().mutable();

        for (int y = entity.blockPosition().getY(); y >= entity.level().getMinY(); y--) {
            entityBlockPosCopy.setY(y);
            BlockState state = entity.level().getBlockState(entityBlockPosCopy);

            if (!state.isAir() && state.getFluidState().isEmpty())
            {
                return y;
            }
        }

        return entity.level().getMinY();
    }

    private static String getDisplayableSpeed(Entity vehicle, boolean includeVerticalSpeed)
    {
        double speedBlocksPerTick;
        if (includeVerticalSpeed)
        {
            speedBlocksPerTick = vehicle.getDeltaMovement().length();
        } else {
            Vec3 deltaMovment = vehicle.getDeltaMovement();
            speedBlocksPerTick = Math.sqrt(deltaMovment.x * deltaMovment.x + deltaMovment.z * deltaMovment.z);
        }

        double speedBlocksPerSecond = speedBlocksPerTick * 20;
        // implement delta tracking
        return (int) speedBlocksPerSecond + "m/s";
    }
}
