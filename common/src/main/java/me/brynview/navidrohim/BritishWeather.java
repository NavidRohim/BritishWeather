package me.brynview.navidrohim;

import me.brynview.navidrohim.server.config.CommonConfig;
import me.brynview.navidrohim.server.weather.ServerWeatherManager;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BritishWeather
{

    private static CommonConfig CONFIG;
    private static ServerWeatherManager WEATHER_MANAGER;

    /*
    Since the config is not present in the common namespace, each mod loader must provide their own config (using YACL, which doesn't have a common JAR).
    Then pass an instance of CommonModConfig to common init so all common code can use it.
     */
    public static void init(@NotNull CommonConfig config, Consumer<MinecraftServer> weatherRefreshCallback)
    {
        CONFIG = config;
        WEATHER_MANAGER = new ServerWeatherManager(weatherRefreshCallback);
    }

    // Config getter
    public static CommonConfig getConfig()
    {
        return CONFIG;
    }
    public static ServerWeatherManager getWeatherManager() {return WEATHER_MANAGER;}

    // This onTick method works on both IntegratedServer and DedicatedServer theoretically.
    // But for LAN worlds, I do not like this.
    public static void onTick(MinecraftServer server)
    {
        long tick = server.getTickCount();

        if (tick == 1)
        {
            // Call on first tick
            WeatherLocationSources.initCachesForMethods(server);
            getWeatherManager().getWeatherChangeCallback().accept(server);
        }

        if (tick % CONFIG.getWeatherFetchIntervalInTicks() == 0 || tick == 1) // Check if tick is multiple of configs update interval, or is 1 (Player / server just started)
        {
            // Also set weather on first tick or when threshold is reached.
            WEATHER_MANAGER.setWeather(server);
        }

        WEATHER_MANAGER.tickHailDamage(server, tick);
    }
}