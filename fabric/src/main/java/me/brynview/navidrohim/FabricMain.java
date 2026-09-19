package me.brynview.navidrohim;

import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.DefaultConfig;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import me.brynview.navidrohim.common.config.FabricConfig;
import me.brynview.navidrohim.common.config.FabricConfigSerializer;
import me.brynview.navidrohim.platform.Services;
import me.brynview.navidrohim.server.CommandDispatcher;
import me.brynview.navidrohim.server.config.CommonConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class FabricMain implements ModInitializer {

    public static boolean hasYacl = false;

    @Override
    public void onInitialize() {

        // init
        initNetwork();
        initParticles();
        CommonConfig configToUse = initConfig();

        // events

        ServerTickEvents.END_SERVER_TICK.register(BritishWeather::onTick);
        ServerPlayerEvents.JOIN.register((player) -> {
            ServerPlayNetworking.send(player, WeatherUpdatePacket.fromCurrentState());
        });
        BritishWeather.init(configToUse, (server) -> {
                WeatherUpdatePacket packet = WeatherUpdatePacket.fromCurrentState();
                server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            CommandDispatcher.registerCommandsForDispatcher(dispatcher);
        });
    }

    private void initParticles()
    {
        ModParticles.HAIL = FabricParticleTypes.simple();
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fallen_hail_1"), ModParticles.HAIL);
    }

    private void initNetwork()
    {
        PayloadTypeRegistry.clientboundPlay().register(WeatherUpdatePacket.TYPE, WeatherUpdatePacket.CODEC);
    }

    private CommonConfig initConfig()
    {
        if (Services.PLATFORM.isModLoaded(Constants.YACL_MOD_ID))
        {
            FabricConfigSerializer.HANDLER.load();
            hasYacl = true;
            return new FabricConfig();
        }
        return new DefaultConfig();
    }
}
