package me.brynview.navidrohim.client;

import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public class ModClientEventBus
{

    @SubscribeEvent
    public static void registerPacketReceiver(RegisterClientPayloadHandlersEvent event)
    {
        event.register(
                WeatherUpdatePacket.TYPE,
                (packet, _) ->
                {
                    ClientCommon.getWeatherManager().setWeather(packet.condition());
                }
        );
    }

    @SubscribeEvent
    public static void registerParticleProvider(RegisterParticleProvidersEvent event)
    {
        ModParticles.HAIL = NeoforgeMainClient.HAIL.get();
        event.registerSpriteSet(ModParticles.HAIL, HailParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerKeybindsEvent(RegisterKeyMappingsEvent event)
    {
        event.registerCategory(ClientKeybinds.CATEGORY);
        event.register(ClientKeybinds.ENTRY_HUD_KEY);
    }
}
