package me.brynview.navidrohim.client.hud.compass.entry.builtin.entrygroup;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VehicleEntryGroup extends DefaultEntryGroup<VehicleEntryGroup.VehicleEntry>
{

    private static final String ROWBOAT = "\uD83D\uDEA3";
    private static final String TRAIN = "\uD83D\uDE86";

    public static final class VehicleEntry extends DefaultEntry
    {
        public final class VehicleRenderer extends EntryRenderer
        {
            @Override
            public void drawRawEntry(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, @NotNull LocalPlayer player, @NotNull Compass compass)
            {
                drawMarker(guiGraphicsExtractor);
                drawSpeed(guiGraphicsExtractor);
                drawDecoration(guiGraphicsExtractor);
            }

            private void drawSpeed(GuiGraphicsExtractor guiGraphicsExtractor)
            {
                Vec3 vel = VehicleEntry.this.vehicle.getDeltaMovement();
                double speedBlocksPerTick = vel.length();
                double speedBlocksPerSecond = speedBlocksPerTick * 20;

                String displayableSpeed = (int) speedBlocksPerSecond + "m/s";
                FontHelper.draw(mc, guiGraphicsExtractor, displayableSpeed, compass.util.centerStringAroundX(displayableSpeed, screenX), compass.util.getYRowOnCompass(screenYRow + 1), getRenderColour(), FontHelper.TextType.VALUE);
            }
        }

        private final VehicleEntity vehicle;

        public VehicleEntry(VehicleEntity entity, String marker)
        {
            super(entity.position(), entity.level().dimension().identifier().toString(), marker);
            this.setRenderer(new VehicleRenderer());
            this.vehicle = entity;
        }

        @Override
        public boolean isBuiltIn()
        {
            return true;
        }
    }

    public VehicleEntryGroup(@Nullable ResourceKey<Level> level)
    {
        super(level);
    }

    @Override
    public void startTick(@NotNull LocalPlayer player)
    {
        GeneralUtils.getEntitiesInView(player, VehicleEntity.class).forEach(entity -> {
            if (!entity.hasPassenger(player))
            {
                String marker = null;
                if (entity instanceof AbstractBoat)
                {
                    marker = ROWBOAT;
                } else if (entity instanceof AbstractMinecart)
                {
                    marker = TRAIN;
                }

                if (marker != null)
                {
                    this.addEntry(new VehicleEntry(entity, marker));
                }
            }
        });
    }

    @Override
    public void tick(@NotNull LocalPlayer player)
    {
        this.getEntries().forEach(entry -> entry.setPosition(entry.vehicle.position()));
    }

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
