package me.brynview.navidrohim;

import me.brynview.navidrohim.client.ModParticles;
import me.brynview.navidrohim.config.FabricCommonModConfig;
import me.brynview.navidrohim.config.FabricNativeModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {

        // init
        FabricNativeModConfig.HANDLER.load();
        initParticles();

        // events
        ServerTickEvents.END_SERVER_TICK.register(CommonClass::onTick);

        CommonClass.init(new FabricCommonModConfig());
    }

    private void initParticles()
    {
        ModParticles.HAIL = FabricParticleTypes.simple();
    }
}
