package me.brynview.navidrohim.common;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/*
Packet sent to client to update the current weather.
 */
public record WeatherUpdatePacket(WeatherCondition condition) implements CustomPacketPayload
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weather_update");
    public static final CustomPacketPayload.Type<WeatherUpdatePacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, WeatherUpdatePacket> CODEC = StreamCodec.composite(WeatherCondition.STREAM_CODEC, WeatherUpdatePacket::condition, WeatherUpdatePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    /*
    Make new weather state packet from the current weather state on the server.
     */
    public static WeatherUpdatePacket fromCurrentState()
    {
        return new WeatherUpdatePacket(BritishWeather.getWeatherManager().getState().getWeatherCondition());
    }
}
