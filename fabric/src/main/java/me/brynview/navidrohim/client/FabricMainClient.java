package me.brynview.navidrohim.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.keybinds.ModKeybinds;
import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class FabricMainClient implements ClientModInitializer

{
    @Override
    public void onInitializeClient()
    {
        ParticleProviderRegistry.getInstance().register(ModParticles.HAIL, HailParticle.Provider::new);

        ClientLoginConnectionEvents.INIT.register((_, _) -> ClientCommon.getWeatherManager().reset());

        ClientPlayNetworking.registerGlobalReceiver(WeatherUpdatePacket.TYPE, ((payload, _) ->
                ClientCommon.getWeatherManager().setWeather(payload.condition())));

        ClientTickEvents.START_LEVEL_TICK.register((_) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            ClientCommon.tickClient(player);
        });

        ModKeybinds.init();
        ClientCommon.init();
    }
}
