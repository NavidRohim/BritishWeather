package me.brynview.navidrohim;

import me.brynview.navidrohim.client.ModParticles;
import me.brynview.navidrohim.platform.services.CommonModConfig;
import me.brynview.navidrohim.server.DamageSources;
import me.brynview.navidrohim.server.DataCache;
import me.brynview.navidrohim.server.WeatherManager;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public class CommonClass {

    private static CommonModConfig CONFIG;
    // Cache is only used if getting lat/long from IP.
    public static DataCache CACHE;

    public static final WeatherManager.WeatherState DEBUG_MASTER_WEATHER_STATE = new WeatherManager.WeatherState(90, WeatherManager.WeatherCondition.HAIL_STAGE_1, "London", 1, 1);

    /*
    Since the config is not present in the common namespace, each mod loader must provide their own config (using YACL, which doesn't have a common JAR).
    Then pass an instance of CommonModConfig to common init so all common code can use it.
     */
    public static void init(CommonModConfig config)
    {
        CONFIG = config;

        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fallen_hail_1"), ModParticles.HAIL);
    }

    // Config getter
    public static CommonModConfig getConfig()
    {
        return CONFIG;
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
            WeatherManager.setWeather(server);
        }

        if (tick % 40 == 0 && WeatherManager.getState() != null && WeatherManager.getState().getWeatherCondition().getHailLevel() >= 0)
        {
            WeatherManager.tick(server);
        }
    }
}