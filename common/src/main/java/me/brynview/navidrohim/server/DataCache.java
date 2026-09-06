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
            Codec.INT.fieldOf("WMOCode").forGetter(v -> v.WMOCode),
            Codec.STRING.fieldOf("name").forGetter(v -> v.name),
            Codec.FLOAT.fieldOf("latitude").forGetter(v -> v.latitude),
            Codec.FLOAT.fieldOf("longitude").forGetter(v -> v.longitude)
    ).apply(ins, DataCache::new));

    private static final SavedDataType<DataCache> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weather_ip_cache"), // path: name of dat file©
                    DataCache::new,
                    CODEC,
                    null
            );

    private String ip;
    private int WMOCode;
    private String name;
    private float latitude;
    private float longitude;

    /*
    This constructor is called when the cache file does not exist.
    These are default values and will eventually be overridden IF IP geolocating is enabled.
     */
    public DataCache()
    {
        this.ip = "192.168.0.1";
        this.WMOCode = 0;
        this.name = Constants.DEFAULT_LOCATION_NAME;
        this.latitude = 0;
        this.longitude = 0;
    }

    /*
    If dat file already exists.
     */
    public DataCache(String ip, int WMOCode, String name, float latitude, float longitude)
    {
        this.ip = ip;
        this.WMOCode = WMOCode;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
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
        if (CommonClass.getCache() == null)
        {
            CommonClass.setCache(server.getDataStorage().computeIfAbsent(TYPE));
            CommonClass.getWeatherManager().setState(CommonClass.getCache().getCachedWeatherState());
            Constants.LOG.debug("Initialized weather cache");
        }
    }

    public void setCachedWeatherState(String ip, ServerWeatherManager.WeatherState weatherState)
    {
        Constants.LOG.info("Caching weather state for IP -> {}", weatherState);

        this.ip = ip;
        this.WMOCode = weatherState.WMOCode;
        this.name = weatherState.location.name();
        this.longitude = weatherState.location.lat();
        this.latitude = weatherState.location.lon();

        this.setDirty(true);
    }

    @Nullable
    public ServerWeatherManager.WeatherState getCachedWeatherState()
    {
        return new ServerWeatherManager.WeatherState(this.WMOCode, this.name, this.latitude, this.longitude);
    }

    public boolean ipNotCached()
    {
        return !this.ip.equalsIgnoreCase(Constants.USER_IP);
    }
}