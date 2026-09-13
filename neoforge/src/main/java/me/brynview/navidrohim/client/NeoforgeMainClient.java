package me.brynview.navidrohim.client;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.config.NeoforgeNativeModConfig;
import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoforgeMainClient
{

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Constants.MOD_ID);

    public static final Supplier<SimpleParticleType> HAIL = PARTICLE_TYPES.register(
            "fallen_hail_1",
            () -> new SimpleParticleType(false));

    public NeoforgeMainClient(IEventBus eventBus, ModContainer modContainer)
    {
        NeoforgeMainClient.PARTICLE_TYPES.register(eventBus);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (_, parent) -> NeoforgeNativeModConfig.getModConfigScreenFactory(parent));
        eventBus.register(ModClientEventBus.class);
        NeoForge.EVENT_BUS.register(this);

        ClientCommon.init();
    }

    @SubscribeEvent
    public void onServerConnect(ClientPlayerNetworkEvent.LoggingIn event)
    {
        ClientCommon.getWeatherManager().reset();
    }

}
