package me.brynview.navidrohim;

import me.brynview.navidrohim.client.particle.ModParticles;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.platform.services.CommonModConfig;
import me.brynview.navidrohim.server.DataCache;
import me.brynview.navidrohim.server.ServerWeatherManager;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class CommonClass {

    private static CommonModConfig CONFIG;
    private static DataCache CACHE; // Cache is only used if getting lat/long from IP.
    private static ServerWeatherManager WEATHER_MANAGER;

    /*
    Since the config is not present in the common namespace, each mod loader must provide their own config (using YACL, which doesn't have a common JAR).
    Then pass an instance of CommonModConfig to common init so all common code can use it.
     */
    public static void init(CommonModConfig config, Consumer<MinecraftServer> weatherRefreshCallback)
    {
        CONFIG = config;
        WEATHER_MANAGER = new ServerWeatherManager(weatherRefreshCallback);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fallen_hail_1"), ModParticles.HAIL);
    }

    // Config getter
    public static CommonModConfig getConfig()
    {
        return CONFIG;
    }
    public static ServerWeatherManager getWeatherManager() {return WEATHER_MANAGER;}
    public static DataCache getCache() { return CACHE; }

    public static void setCache(DataCache CACHE)
    {
        if (CommonClass.CACHE == null)
        {
            CommonClass.CACHE = CACHE;
        } else {
            throw new IllegalStateException("Cache already set.");
        }
    }

    // This onTick method works on both IntegratedServer and DedicatedServer theoretically.
    // But for LAN worlds, I do not like this.
    public static void onTick(MinecraftServer server)
    {
        long tick = server.getTickCount();

        if (tick == 1)
        {
            // Call on first tick
            DataCache.initCache(server);
        }

        if (tick % CONFIG.getWeatherFetchIntervalInTicks() == 0 || tick == 1) // Check if tick is multiple of configs update interval, or is 1 (Player / server just started)
        {
            // Also set weather on first tick or when threshold is reached.
            WEATHER_MANAGER.setWeather(server);
        }

        WEATHER_MANAGER.getState().ifPresent((condition) -> {
            if (condition.getWeatherCondition().isHail() && tick % (20L * condition.getWeatherCondition().getHailLevel()) == 0)
            {
                WEATHER_MANAGER.tick(server);
            }
        });
    }
}