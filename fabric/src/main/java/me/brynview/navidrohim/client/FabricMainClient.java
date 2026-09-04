package me.brynview.navidrohim.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EndRodParticle;

public class FabricMainClient implements ClientModInitializer

{
    @Override
    public void onInitializeClient()
    {
        ClientTickEvents.START_LEVEL_TICK.register(ClientCommon::clientLevelTick);
        ParticleProviderRegistry.getInstance().register(ModParticles.HAIL, HailParticle.Provider::new);
    }
}
