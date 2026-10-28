package me.brynview.navidrohim.client;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.particle.HailParticle;
import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public class FabricMainClient implements ClientModInitializer
{
    private float tickCount = 0;

    @Override
    public void onInitializeClient()
    {
        ParticleProviderRegistry.getInstance().register(ModParticles.HAIL, HailParticle.Provider::new);
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "compass_hud"), this.compassHudLayer());

        // Reset weather manager upon joining server
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCommon.leaveServerEvent());
        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> ClientCommon.joinServerEvent());

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

    private HudElement compassHudLayer()
    {
        return (gpu) ->
        {
            tickCount += gpu.partialTicks();

            if (!BritishWeather.getConfig().shouldRenderCompass())
            {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            Compass.getInstance().render(gpu, scaledWidth, tickCount, BritishWeather.getConfig().getCompassY(), false);
        };
    }
    private void initKeybinds()
    {
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.DEBUG_ON_PRESS);

        KeyMappingHelper.registerKeyMapping(ClientKeybinds.ENTRY_HUD_KEY);
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.ZOOM_MODIFIER);

        KeyMappingHelper.registerKeyMapping(ClientKeybinds.DELETION);
        KeyMappingHelper.registerKeyMapping(ClientKeybinds.CREATE);

        KeyMappingHelper.registerKeyMapping(ClientKeybinds.SNAP_TO_NEAREST);
    }
}
