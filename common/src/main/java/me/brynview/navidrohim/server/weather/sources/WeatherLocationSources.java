package me.brynview.navidrohim.server.weather.sources;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.weather.WeatherCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Map;
import java.util.Set;

public class WeatherLocationSources
{

    public static final IPLocationSource IP = new IPLocationSource();
    public static final WeatherLocationSource.ManualLocationSource MANUAL = new WeatherLocationSource.ManualLocationSource();
    public static final PostcodeLocationSource POSTCODE = new PostcodeLocationSource();

    private static final Map<String, WeatherLocationSource> sources = Map.of(
            IP.getIdentifier(), IP,
            MANUAL.getIdentifier(), MANUAL,
            POSTCODE.getIdentifier(), POSTCODE
    );

    public static void initCachesForMethods(MinecraftServer minecraftServer)
    {
        for (WeatherLocationSource source : sources.values())
        {
            SavedDataType<WeatherCache> type = WeatherCache.getTypeForMethod(source);
            if (type != null)
            {
                source.setCache(minecraftServer.getDataStorage().computeIfAbsent(type));
            }
        }
    }

    public static WeatherLocationSource getSource(String type)
    {
        WeatherLocationSource source = sources.get(type);
        if (source != null)
        {
            return source;
        }
        Constants.LOG.warn("No source named \"{}\" will use default source \"{}\". Valid source values are {}", type, Constants.DefaultConfigValues.defaultLocationMethod.getIdentifier(), getSources());
        return Constants.DefaultConfigValues.defaultLocationMethod;
    }

    public static Set<String> getSources()
    {
        return sources.keySet();
    }
}
