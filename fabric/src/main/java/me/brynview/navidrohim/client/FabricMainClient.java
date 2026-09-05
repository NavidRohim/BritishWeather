package me.brynview.navidrohim.client;

import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public class FabricMainClient implements ClientModInitializer

{
    @Override
    public void onInitializeClient()
    {
        ParticleProviderRegistry.getInstance().register(ModParticles.HAIL, HailParticle.Provider::new);
        ClientPlayNetworking.registerGlobalReceiver(WeatherUpdatePacket.TYPE, ((payload, context) ->
        {
            ClientCommon.getWeatherManager().setWeather(payload.condition());
        }));

        ClientCommon.init();
    }
}
