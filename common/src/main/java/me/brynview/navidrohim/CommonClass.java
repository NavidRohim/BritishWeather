package me.brynview.navidrohim;

import me.brynview.navidrohim.platform.services.CommonModConfig;
import me.brynview.navidrohim.server.WeatherManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as NeoForge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    private static CommonModConfig CONFIG;

    /*
    Since the config is not present in the common namespace, each mod loader must provide their own config (using YACL, which doesn't have a common JAR).
    Then pass an instance of CommonModConfig to common init so all common code can use it.
     */
    public static void init(CommonModConfig config)
    {
        CONFIG = config;
    }

    public static CommonModConfig getConfig()
    {
        return CONFIG;
    }

    public static void onTick(MinecraftServer server)
    {
        long tick = server.getTickCount();

        if (tick % CONFIG.getWeatherFetchIntervalInTicks() == 0 || tick == 1) // Check if tick is multiple of configs update interval, or is 1 (Player / server just started)
        {
            WeatherManager.fetchNewWeatherState(server);
        }
    }

    /*
    Should only be fired when minecraft is started.
     */
    public static void onStart(Minecraft _minecraft)
    {
        WeatherManager.setCoordinatesFromIP();
    }
}