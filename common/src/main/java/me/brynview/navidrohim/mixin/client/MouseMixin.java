package me.brynview.navidrohim.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.brynview.navidrohim.client.ClientCommon;
import net.minecraft.client.MouseHandler;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin
{
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"), cancellable = true)
    private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci, @Local(name = "wheelXY") Vector2i wheelXY)
    {
        double cMouse = wheelXY.y;
        boolean scrollingUp = cMouse <= -1.0;
        boolean scrollingDown = cMouse >= 1.0;

        boolean didScroll = ClientCommon.scrollEvent(scrollingUp, scrollingDown);

        if (didScroll)
        {
            ci.cancel();
        }
    }
}
