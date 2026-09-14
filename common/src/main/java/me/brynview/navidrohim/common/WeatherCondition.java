package me.brynview.navidrohim.common;

import io.netty.buffer.ByteBuf;
import me.brynview.navidrohim.Constants;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Charsets;

public enum WeatherCondition
{
    DEFAULT("ERROR"),
    CLOUDY("cloudy"),
    RAINY("rainy"),
    THUNDERSTORM("thunder"),
    THUNDERSTORM_NO_RAIN("thunder"),

    HAIL_STAGE_1(1, "heavy hail"), // Most intense
    HAIL_STAGE_2(2, "medium hail"),
    HAIL_STAGE_3(3, "light hail"); // Light

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
    final String displayName;

    WeatherCondition(int hailLevel, String displayName)
    {
        this.hailLevel = hailLevel;
        this.weatherTexture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/environment/hail%s.png".formatted(hailLevel));
        this.displayName = displayName;
    }

    WeatherCondition(String displayName)
    {
        this.hailLevel = -1;
        this.weatherTexture = null;
        this.displayName = displayName;
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

    public String getDisplayName()
    {
        return displayName;
    }
}
