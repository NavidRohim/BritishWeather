package me.brynview.navidrohim.server.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/*
CACHE only used when IP geolocating.

 I want CACHE to be independent of the world file (not stored with SavedData, which uses the world folder to save data. Not persistent through different worlds)
 I don't believe there is a Minecraft solution for this. Maybe have to make my own cache system.
 */
public class WeatherCache extends SavedData
{

    public record TypeAndCodec(SavedDataType<WeatherCache> type, Codec<WeatherCache> codec) {}

    public static @Nullable TypeAndCodec getTypeAndCodecForMethod(WeatherLocationSource source)
    {
        if (source.shouldHaveCache())
        {
            Codec<WeatherCache> codec =  RecordCodecBuilder.create(ins -> ins.group(
                    // Values to be stored in cache
                    Codec.STRING.fieldOf("key").forGetter(v -> v.key), // IP. Acts as a key
                    Codec.INT.fieldOf("WMOCode").forGetter(v -> v.WMOCode),
                    Codec.STRING.fieldOf("name").forGetter(v -> v.name),
                    Codec.FLOAT.fieldOf("latitude").forGetter(v -> v.latitude),
                    Codec.FLOAT.fieldOf("longitude").forGetter(v -> v.longitude)
            ).apply(ins, WeatherCache::new));

            SavedDataType<WeatherCache> type = new SavedDataType<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, source.getKey() + "_cache"), // path: name of dat file©
                    WeatherCache::new,
                    codec,
                    null
            );
            return new TypeAndCodec(type, codec);
        }
        return null;
    }

    private String key;
    private int WMOCode;
    private String name;
    private float latitude;
    private float longitude;

    /*
    This constructor is called when the cache file does not exist.
    These are default values and will eventually be overridden IF IP geolocating is enabled.
     */
    public WeatherCache()
    {
        this.key = "192.168.0.1";
        this.WMOCode = 0;
        this.name = Constants.DEFAULT_LOCATION_NAME;
        this.latitude = 0;
        this.longitude = 0;
    }

    /*
    If dat file already exists.
     */
    public WeatherCache(String key, int WMOCode, String name, float latitude, float longitude)
    {
        this.key = key;
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

    public void setCachedWeatherState(String key, ServerWeatherManager.WeatherState weatherState)
    {
        this.key = key;
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

    public boolean isNotCached(String key)
    {
        return !this.key.equalsIgnoreCase(key);
    }

    public String getKey()
    {
        return key;
    }
}