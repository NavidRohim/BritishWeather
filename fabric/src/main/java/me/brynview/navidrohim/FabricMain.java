package me.brynview.navidrohim;

import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import me.brynview.navidrohim.common.config.FabricCommonModConfig;
import me.brynview.navidrohim.common.config.FabricNativeModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {

        // init
        initNetwork();
        initParticles();
        FabricNativeModConfig.HANDLER.load();

        // events
        ServerTickEvents.END_SERVER_TICK.register(CommonClass::onTick);
        CommonClass.init(new FabricCommonModConfig(), (server) -> {
                WeatherUpdatePacket packet = new WeatherUpdatePacket(CommonClass.getWeatherManager().getState().getWeatherCondition());
                server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
        });
    }

    private void initParticles()
    {
        ModParticles.HAIL = FabricParticleTypes.simple();
    }

    private void initNetwork()
    {
        PayloadTypeRegistry.clientboundPlay().register(WeatherUpdatePacket.TYPE, WeatherUpdatePacket.CODEC);
    }
}
