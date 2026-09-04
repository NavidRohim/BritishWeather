package me.brynview.navidrohim.mixin;

import com.mojang.blaze3d.systems.RenderPass;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.server.WeatherManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin
{

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void injectTest(RenderPass renderPass, AbstractTexture texture, int startColumn, int columnCount, CallbackInfo ci)
    {
        WeatherManager.WeatherCondition condition = ClientCommon.SERVER_WEATHER_STATE.getWeatherCondition();

        if (condition.isHail() && startColumn == 0) // startColumn = 0 makes sure it's raining and not snowing.
        {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            AbstractTexture hailTexture = textureManager.getTexture(condition.getWeatherTexture()); // if isHail is true, this warning doesn't matter

            renderPass.bindTexture("Sampler0", hailTexture.getTextureView(), hailTexture.getSampler());
            renderPass.drawIndexed(columnCount * 6, 1, 0, 0, 0);
            ci.cancel();
        }
    }
}
