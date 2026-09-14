package me.brynview.navidrohim.server;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.common.WeatherUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModEventBus
{
    @SubscribeEvent
    public static void onServerPostTick(ServerTickEvent.Pre event)
    {
        BritishWeather.onTick(event.getServer());
    }

    @SubscribeEvent
    public static void registerPacket(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(WeatherUpdatePacket.TYPE, WeatherUpdatePacket.CODEC);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer)
        {
            ((ServerPlayer) player).connection.send(WeatherUpdatePacket.fromCurrentState());
        }
    }

    @SubscribeEvent
    public static void registerServerCommandsEvent(RegisterCommandsEvent event)
    {
        BritishWeather.registerCommandsForServer(event.getDispatcher());
    }
}
