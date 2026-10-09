package me.brynview.navidrohim.mixin.client;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin
{
    /*
    @Unique
    private static AbstractTexture britishweather$hailTexture;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    private @Nullable AbstractTexture rainTexture;

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void injectHailEffect(RenderPass renderPass, AbstractTexture texture, int startColumn, int columnCount, CallbackInfo ci)
    {
        WeatherCondition condition = ClientCommon.getWeatherManager().getWeather();

        // Check if it's hailing, and we are in an area that will hail (not snowing)
        // By checking if the current texture for the weather is the rain.
        if (condition.isHail() && rainTexture == texture)
        {
            // Not sure what the following lines do. I am not versed in rendering
            renderPass.setUniform("Sampler0", britishweather$hailTexture.getTextureView(), britishweather$hailTexture.getSampler());
            renderPass.drawIndexed(columnCount * 6, 1, 0, 0, 0);
            ci.cancel();
        }
    }

    /*
    Correct stage to load the hail texture.

    @Inject(method = "prepare", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem$AutoStorageIndexBuffer;requestIndexCount(I)V"))
    private void prepareHailTexture(CallbackInfo ci)
    {
        WeatherCondition condition = ClientCommon.getWeatherManager().getWeather();
        if (condition.isHail())
        {
            britishweather$hailTexture = textureManager.getTexture(condition.getWeatherTexture());
        }
    }
    */
}
