package me.brynview.navidrohim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GeneralUtils
{
    // Math constants used for rendering
    public static final int DETECTION_DISTANCE = 25; // How far to check in front of the player for entities

    public static String getDimensionFromPlayer(@NotNull LocalPlayer player)
    {
        return player.level().dimension().identifier().toString();
    }

    public static <T extends Entity> List<T> getEntitiesInView(@NotNull LocalPlayer player, @NotNull Class<T> entityClass, float partialTicks)
    {
        Vec3 look = player.getViewVector(partialTicks);
        AABB normalBB = player.getBoundingBox();
        AABB inFrontOfPlayer = normalBB.expandTowards(look.x, 0, look.z).inflate(DETECTION_DISTANCE);

        // For every LivingEntity (includes all living things but not items or arrows) except current player. Does include armour stands
        return player.level().getEntitiesOfClass(entityClass, inFrontOfPlayer, (en) -> en != player && en != player.getVehicle());
    }

    public static void renderFillForElement(GuiGraphicsExtractor renderer, LayoutElement element, int colour)
    {
        renderer.fill(element.getX(), element.getY(), element.getX() + element.getWidth(), element.getY() + element.getHeight(), colour);
    }

    public static FormattedCharSequence getFormattedChar(char c)
    {
        return (out) -> out.accept(1, Style.EMPTY, c);
    }

    public static void setScreenAndShowNoFlicker(Screen screen)
    {
        try (Zone ignored = Profiler.get().zone("forcedTick"))
        {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.setScreen(screen);
        }
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
}
