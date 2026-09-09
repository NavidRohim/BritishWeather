package me.brynview.navidrohim.server.weather.sources;

import me.brynview.navidrohim.BritishWeather;
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
            if (source.hasCache() && source.getCache() == null)
            {
                source.setCache(minecraftServer.getDataStorage().computeIfAbsent(source.getTypeAndCodec().type()));
            }
        }
        BritishWeather.setCache(BritishWeather.getConfig().getLocationSource().getCache());
        BritishWeather.getWeatherManager().setState(BritishWeather.getConfig().getLocationSource().getCache().getCachedWeatherState());
    }

    public static WeatherLocationSource getSource(String type)
    {
        return sources.get(type);
    }
}
