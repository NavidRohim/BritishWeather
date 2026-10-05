package me.brynview.navidrohim.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.event.MouseEvent;

@Mixin(MouseHandler.class)
public class MouseMixin
{
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"), cancellable = true)
    private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci, @Local Vector2i wheelXY)
    {
        if (ClientCommon.ZOOM_MODIFIER.isDown())
        {
            GeneralUtils.doHouseEventHandleFromMixinToAvoidReloadingEveryDebugSessionForFuckSake(wheelXY);
            ci.cancel();
        }
    }
}
