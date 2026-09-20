package me.brynview.navidrohim.client.gui.compass.providers;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface CompassProvider
{
    Identifier getImage();

    String getText();

    Vec3 getPosition(@NotNull LocalPlayer player);

    boolean shouldShow();

    void tick(@NotNull LocalPlayer player);

}
