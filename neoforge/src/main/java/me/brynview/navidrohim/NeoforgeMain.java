package me.brynview.navidrohim;

import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import me.brynview.navidrohim.server.config.NeoforgeCommonModConfig;
import me.brynview.navidrohim.server.config.NeoforgeNativeModConfig;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MOD_ID)
public class NeoforgeMain
{
    public NeoforgeMain(ModContainer modContainer)
    {
        NeoforgeNativeModConfig.HANDLER.load();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (_, parent) -> NeoforgeNativeModConfig.getModConfigScreenFactory(parent));

        BritishWeather.init(new NeoforgeCommonModConfig(), (server) ->
        {
            WeatherUpdatePacket packet = new WeatherUpdatePacket(BritishWeather.getWeatherManager().getState().getWeatherCondition());
            server.getPlayerList().broadcastAll(packet);
        });
    }
}