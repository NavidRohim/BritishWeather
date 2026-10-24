package me.brynview.navidrohim.util;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.hud.compass.Compass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;
import java.util.stream.Stream;

public class GeneralUtils
{
    // Math constants used for rendering
    public static final double EXPAND_INFLATE_WITH_FOV_CONST = 0.119; // value at lowest fov (30): 3.477 blocks value at highest (110): 13.07
    public static final int DETECTION_DISTANCE = 40; // How far to check in front of the player for entities
    public static final int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10; // Max entities allowed on compass

    public static String getDimensionFromPlayer(@NotNull LocalPlayer player)
    {
        return player.level().dimension().identifier().toString();
    }

    public static <T extends Entity> Stream<T> getEntitiesInView(@NotNull LocalPlayer player, @NotNull Class<T> entityClass)
    {
        Minecraft mc = Minecraft.getInstance();
        Vec3 look = player.getViewVector(0).scale(DETECTION_DISTANCE);

        int fov = mc.options.fov().get();

        AABB normalBB = player.getBoundingBox();
        AABB inFrontOfPlayer = normalBB
                .expandTowards(look.x, 0, look.z)
                .inflate(EXPAND_INFLATE_WITH_FOV_CONST * fov); // inflate so entities don't get cut off from compass

        // For every LivingEntity (includes all living things but not items or arrows) except current player. Does include armour stands
        return mc.level.getEntitiesOfClass(entityClass, inFrontOfPlayer, (en) -> (en != player.asLivingEntity() && !en.getBoundingBox().intersects(normalBB)))
                .stream()
                .limit(MAX_ALLOWED_ENTITIES_ON_COMPASS);
    }

    public static void renderFillForElement(GuiGraphicsExtractor renderer, LayoutElement element, int colour)
    {
        renderer.fill(element.getX(), element.getY(), element.getX() + element.getWidth(), element.getY() + element.getHeight(), colour);
    }


}
