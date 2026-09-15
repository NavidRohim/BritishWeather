package me.brynview.navidrohim.mixin.client;

import com.mojang.renderpearl.api.commands.RenderPass;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.common.WeatherCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
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

    @Unique
    private static AbstractTexture britishweather$hailTexture;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    private @Nullable AbstractTexture rainTexture;

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void injectTest(RenderPass renderPass, AbstractTexture texture, int startColumn, int columnCount, CallbackInfo ci)
    {
        WeatherCondition condition = ClientCommon.getWeatherManager().getWeather();
        if (condition.isHail() && rainTexture == texture)
        {
            renderPass.setUniform("Sampler0", britishweather$hailTexture.getTextureView(), britishweather$hailTexture.getSampler());
            renderPass.drawIndexed(columnCount * 6, 1, 0, 0, 0);
            ci.cancel();
        }
    }

    @Inject(method = "prepare", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem$AutoStorageIndexBuffer;requestIndexCount(I)V"))
    private void prepareTexture(CallbackInfo ci)
    {
        WeatherCondition condition = ClientCommon.getWeatherManager().getWeather();
        if (condition.isHail())
        {
            britishweather$hailTexture = textureManager.getTexture(condition.getWeatherTexture());
        }
    }
}
