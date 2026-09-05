package me.brynview.navidrohim.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/*
CACHE only used when IP geolocating.

 I want CACHE to be independent of the world file (not stored with SavedData, which uses the world folder to save data. Not persistent through different worlds)
 I don't believe there is a Minecraft solution for this. Maybe have to make my own cache system.
 */
public class DataCache extends SavedData
{
    private static final Codec<DataCache> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            // Values to be stored in cache
            Codec.STRING.fieldOf("ip").forGetter(v -> v.ip), // IP. Acts as a key
            Codec.STRING.fieldOf("weather_cache").forGetter(v -> v.rawWeatherCache) // Serialized WeatherState instance
    ).apply(ins, DataCache::new));

    public static final SavedDataType<DataCache> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weather_ip_cache"), // path: name of dat file
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

    /*
    This method is called on the first tick of a new world load.
    For servers, this works "fine" for clients, this works, but not how I want it to.
    If the cache needs to be initialised, I want it to be when the minecraft menu first appears,
    not when a world is loaded. Because again, as stated in some other comments, I want the cache to be
    independent of the world folder.
     */
    public static void initCache(MinecraftServer server)
    {
        if (CommonClass.CACHE == null)
        {
            CommonClass.CACHE = server.getDataStorage().computeIfAbsent(TYPE);
            CommonClass.getWeatherManager().setState(CommonClass.CACHE.getCachedWeatherState());
            Constants.LOG.debug("Initialized weather cache");
        } else {
            Constants.LOG.debug("Cache already been initialized. Ignoring");
        }
    }

    public void setCachedWeatherState(String ip, ServerWeatherManager.WeatherState weatherState)
    {
        Constants.LOG.debug("Caching weather state for IP -> {}", weatherState);

        this.ip = ip;
        this.rawWeatherCache = weatherState.serialize();
        this.setDirty(true);
    }

    public ServerWeatherManager.WeatherState getCachedWeatherState()
    {
        return ServerWeatherManager.WeatherState.deserialize(rawWeatherCache);
    }

    public boolean isIpCached()
    {
        return this.ip.equals(Constants.USER_IP);
    }
}
