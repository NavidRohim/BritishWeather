package me.brynview.navidrohim.client;

import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.minecraft.client.ClientClockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

public class FabricMainClient implements ClientModInitializer

{
    @Override
    public void onInitializeClient()
    {
        ParticleProviderRegistry.getInstance().register(ModParticles.HAIL, HailParticle.Provider::new);

        // Reset weather manager upon joining server
        ClientLoginConnectionEvents.INIT.register((_, _) -> ClientCommon.joinServerEvent());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCommon.leaveServerEvent());

        // Client packet registry
        ClientPlayNetworking.registerGlobalReceiver(WeatherUpdatePacket.TYPE, ((payload, _) ->
                ClientCommon.getWeatherManager().setWeather(payload.condition())));

        // For loading compass entries
        // For ticking compass and what the compass relies on (TimeMethods, entry ticking)
        ClientTickEvents.START_LEVEL_TICK.register((_) -> {
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
