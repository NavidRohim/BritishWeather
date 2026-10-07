package me.brynview.navidrohim.client;

import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class FabricMainClient implements ClientModInitializer

{
    @Override
    public void onInitializeClient()
    {
        ParticleProviderRegistry.getInstance().register(ModParticles.HAIL, HailParticle.Provider::new);

        // Reset weather manager upon joining server
        ClientLoginConnectionEvents.INIT.register((_, _) -> ClientCommon.getWeatherManager().reset());

        // Client packet registry
        ClientPlayNetworking.registerGlobalReceiver(WeatherUpdatePacket.TYPE, ((payload, _) ->
                ClientCommon.getWeatherManager().setWeather(payload.condition())));

        // For loading compass entries
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((_, _) -> ClientCommon.changeLevel());

        // For ticking compass and what the compass relies on (TimeMethods, entry ticking)
        ClientTickEvents.START_LEVEL_TICK.register((lvl) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            ClientCommon.tickClient(player);
        });

        initKeybinds();

        ClientCommon.init();
    }

    private void initKeybinds()
    {
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.DEBUG_ON_PRESS);

        KeyMappingHelper.registerKeyMapping(ClientKeybinds.ENTRY_HUD_KEY);
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.ZOOM_MODIFIER);

        KeyMappingHelper.registerKeyMapping(ClientKeybinds.DELETION);
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.CREATE);

        KeyMappingHelper.registerKeyMapping(ClientKeybinds.LEFT_SNAP);
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.RIGHT_SNAP);
    }
}
