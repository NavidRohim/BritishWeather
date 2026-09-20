package me.brynview.navidrohim.mixin.client;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.gui.compass.HudCompass;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin
{
    @Shadow
    private int tickCount;

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractBossOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void injectHudCompass(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        if (!BritishWeather.getConfig().shouldRenderCompass())
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        HudCompass.drawState(graphics, mc, scaledWidth, tickCount);
    }
}
