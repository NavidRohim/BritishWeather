package me.brynview.navidrohim.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

public class DataCache extends SavedData
{
    private static final Codec<DataCache> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            Codec.STRING.fieldOf("ip").forGetter(v -> v.ip),
            Codec.STRING.fieldOf("weather_cache").forGetter(v -> v.rawWeatherCache)
    ).apply(ins, DataCache::new));

    public static final SavedDataType<DataCache> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "data_cache"),
                    DataCache::new,
                    CODEC,
                    null
            );

    private String ip;
    private String rawWeatherCache;

    public DataCache(String ip, String rawWeatherCache)
    {
        this.ip = ip;
        this.rawWeatherCache = rawWeatherCache;
    }

    /*
    This constructor is called when the cache file does not exist.
    These are default values and will eventually be overridden IF IP geolocating is enabled.
     */
    public DataCache()
    {
        this.ip = "192.168.0.1";
        this.rawWeatherCache = "{}";
    }

    public static void initCache(MinecraftServer server)
    {
        CommonClass.CACHE = server.getDataStorage().computeIfAbsent(TYPE);
        WeatherManager.setState(CommonClass.CACHE.getCachedWeatherState());
    }

    public void setCachedWeatherState(String ip, WeatherManager.WeatherState weatherState)
    {
        Constants.LOG.info("Caching weather state from IP: {}", weatherState.toString());
        this.ip = ip;
        this.rawWeatherCache = weatherState.serialize();
        this.setDirty(true);
    }

    public WeatherManager.WeatherState getCachedWeatherState()
    {
        WeatherManager.WeatherState state = WeatherManager.WeatherState.deserialize(rawWeatherCache);
        Constants.LOG.info("Cache hit on IP weather state: {}", state);
        return state;
    }

    public boolean isIpCached()
    {
        return this.ip.equals(Constants.USER_IP);
    }
}
