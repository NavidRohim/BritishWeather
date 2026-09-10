package me.brynview.navidrohim.server.weather.sources;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.server.weather.WeatherCache;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

public class WeatherLocationSources
{
    private static final Map<String, WeatherLocationSource> sources = Map.of(
            "ip", new IPLocationSource(),
            "manual", new WeatherLocationSource.ManualLocationSource(),
            "postcode", new PostcodeLocationSource()
    );

    public static void initCachesForMethods(MinecraftServer minecraftServer)
    {
        for (WeatherLocationSource source : sources.values())
        {
            WeatherCache.TypeAndCodec TYPE_AND_CODEC = WeatherCache.getTypeAndCodecForMethod(source);
            if (TYPE_AND_CODEC != null)
            {
                source.setCache(minecraftServer.getDataStorage().computeIfAbsent(TYPE_AND_CODEC.type()));
            }
        }
    }

    public static WeatherLocationSource getSource(String type)
    {
        return sources.get(type);
    }
}
