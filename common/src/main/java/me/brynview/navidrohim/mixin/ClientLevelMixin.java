package me.brynview.navidrohim.mixin;

import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.ModParticles;
import me.brynview.navidrohim.server.WeatherManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin
{
    @ModifyVariable(method = "tickWeatherEffects", at = @At(value = "STORE"), name = "particleType")
    private ParticleOptions changePO(ParticleOptions particleType)
    {
        return particleType == ParticleTypes.RAIN && ClientCommon.SERVER_WEATHER_STATE.getWeatherCondition().isHail() ? ModParticles.HAIL : particleType;
    }
}
