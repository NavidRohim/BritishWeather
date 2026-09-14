package me.brynview.navidrohim;

import com.mojang.brigadier.CommandDispatcher;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.server.config.CommonMLConfig;
import me.brynview.navidrohim.server.weather.WeatherCache;
import me.brynview.navidrohim.server.weather.ServerWeatherManager;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class BritishWeather
{

    private static CommonMLConfig CONFIG;
    private static ServerWeatherManager WEATHER_MANAGER;

    /*
    Since the config is not present in the common namespace, each mod loader must provide their own config (using YACL, which doesn't have a common JAR).
    Then pass an instance of CommonModConfig to common init so all common code can use it.
     */
    public static void init(CommonMLConfig config, Consumer<MinecraftServer> weatherRefreshCallback)
    {
        CONFIG = config;
        WEATHER_MANAGER = new ServerWeatherManager(weatherRefreshCallback);
    }

    // Config getter
    public static CommonMLConfig getConfig()
    {
        return CONFIG;
    }
    public static ServerWeatherManager getWeatherManager() {return WEATHER_MANAGER;}
    public static WeatherCache getCache() { return CONFIG.getLocationSource().getCache(); }

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

        WEATHER_MANAGER.tick(server, tick);
    }

    public static void registerCommandsForServer(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("brweather")
                .executes(cmd ->
                {
                    ServerWeatherManager.WeatherState weatherState = BritishWeather.getWeatherManager().getState();
                    String weatherStr = weatherState.getWeatherCondition().getDisplayName();
                    String location = weatherState.getLocation().name();

                    Component message = BritishWeather.getConfig().shouldShowLocationToClients()
                            ? Component.translatable("br.command.brweather.message", weatherStr, location)
                            : Component.translatable("br.command.brweather.message_no_location", weatherStr);

                    // Check if executor is a player.
                    cmd.getSource().sendSuccess(() -> message, false);
                    return 0;
                })
        );
    }
}