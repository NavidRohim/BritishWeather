package me.brynview.navidrohim.mixin.client;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.gui.compass.Compass;
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

    /*
    Inject compass rendering when the boss overlay is rendered.

    This is good because when the boss overlay is rendered, all checks for screen behaviour and other HUD elements have been done,
    so I do not have to do them myself.
     */
    @Inject(method = "extractBossOverlay", at = @At("TAIL"))
    private void injectHudCompass(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        if (!BritishWeather.getConfig().shouldRenderCompass())
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        Compass.getInstance().render(graphics, mc, scaledWidth, tickCount);
    }
}
