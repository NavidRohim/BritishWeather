package me.brynview.navidrohim.client;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public class ModClientEventBus
{
    @SubscribeEvent
    public static void registerPacketReceiver(RegisterClientPayloadHandlersEvent event)
    {
        event.register(
                WeatherUpdatePacket.TYPE,
                (packet, ctx) ->
                {
                    ClientCommon.getWeatherManager().setWeather(packet.condition());
                }
        );
    }

    @SubscribeEvent
    public static void registerParticleProvider(RegisterParticleProvidersEvent event)
    {
        ModParticles.HAIL = NFParticleTypes.HAIL.get();
        event.registerSpriteSet(ModParticles.HAIL, HailParticle.Provider::new);

    }

}
