package me.brynview.navidrohim.common;

import io.netty.buffer.ByteBuf;
import me.brynview.navidrohim.Constants;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Charsets;

public enum WeatherCondition
{
    DEFAULT,
    CLOUDY,
    RAINY,
    THUNDERSTORM,
    THUNDERSTORM_NO_RAIN,

    HAIL_STAGE_1(1), // Most intense
    HAIL_STAGE_2(2),
    HAIL_STAGE_3(3); // Light

    public static final StreamCodec<ByteBuf, WeatherCondition> STREAM_CODEC = StreamCodec.of((b, w) -> b.writeBytes(w.toString().getBytes()), (b) -> {
        String conString = b.readString(b.readableBytes(), Charsets.UTF_8);
        try
        {
            return WeatherCondition.valueOf(conString);
        } catch (IllegalArgumentException e)
        {
            Constants.LOG.error("Server sent unknown weather condition: {}! Usually indicates client-server version mismatch!", conString);
        }
        return WeatherCondition.CLOUDY;
    });

    @Nullable
    final Identifier weatherTexture;
    final int hailLevel;

    WeatherCondition(int hailLevel)
    {
        this.hailLevel = hailLevel;
        this.weatherTexture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/environment/hail%s.png".formatted(hailLevel));
    }

    WeatherCondition()
    {
        this.hailLevel = -1;
        this.weatherTexture = null;
    }

    public boolean isHail()
    {
        return hailLevel > 0;
    }

    public int getHailLevel()
    {
        return hailLevel;
    }

    public @Nullable Identifier getWeatherTexture()
    {
        return weatherTexture;
    }
}
