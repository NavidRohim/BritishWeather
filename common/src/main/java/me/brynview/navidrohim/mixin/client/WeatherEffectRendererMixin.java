package me.brynview.navidrohim.mixin.client;

import com.mojang.blaze3d.systems.RenderPass;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.common.WeatherCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin
{

    @Shadow
    @Final
    private static Identifier RAIN_LOCATION;

    @Unique
    private static final String britishWeather$RAIN = "rain.png";

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void injectTest(RenderPass renderPass, AbstractTexture texture, int startColumn, int columnCount, CallbackInfo ci)
    {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture rainTexture = textureManager.getTexture(RAIN_LOCATION);

        WeatherCondition condition = ClientCommon.getWeatherManager().getWeather();
        if (condition.isHail() && rainTexture == texture)
        {
            AbstractTexture hailTexture = textureManager.getTexture(condition.getWeatherTexture()); // if isHail is true, this warning doesn't matter

            renderPass.bindTexture("Sampler0", hailTexture.getTextureView(), hailTexture.getSampler());
            renderPass.drawIndexed(columnCount * 6, 1, 0, 0, 0);
            ci.cancel();
        }
    }
}
