package me.brynview.navidrohim.server;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.server.weather.ServerWeatherManager;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.Arrays;

public class CommandDispatcher
{
    public static void registerCommandsForDispatcher(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("brweather") // Base
                        // Change BR weather (debug only)
                        .then(Commands.literal("change").requires((c) -> !c.isPlayer() || c.getPlayer().permissions().hasPermission(Permissions.COMMANDS_ADMIN)).then(Commands.argument("weather_condition", StringArgumentType.string()).executes(CommandDispatcher::changeWeather)))
                        // Get current BR weather
                        .then(Commands.literal("get").executes(CommandDispatcher::getWeather))
                        .then(Commands.literal("refresh").executes(CommandDispatcher::refreshWeather)));
    }

    private static int changeWeather(CommandContext<CommandSourceStack> commandSourceStackCommandContext)
    {
        // debug check
        if (!BritishWeather.getConfig().debug())
        {
            return 1;
        }

        String weatherType = commandSourceStackCommandContext.getArgument("weather_condition", String.class);
        try
        {
            {
                // Get weather condition from WeatherCondition enum
                WeatherCondition weatherCondition = WeatherCondition.valueOf(weatherType);

                // Set new state, sent weather packet to all clients. Will be overridden when the next weather check is done though. Which is why it's debug only
                BritishWeather.getWeatherManager().setState(new ServerWeatherManager.WeatherState(0, weatherCondition, BritishWeather.getWeatherManager().getState().getLocation()));
                BritishWeather.getWeatherManager().changeServerWeather(commandSourceStackCommandContext.getSource().getServer(), true);
                commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Changed weather to %s".formatted(weatherCondition.toString())), false);

            }
        } catch (IllegalArgumentException e) // Invalid weather condition
        {
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal("Invalid weather condition. Options are %s".formatted(Arrays.stream(WeatherCondition.values()).toList())));
        }

        return 0;
    }

    private static int refreshWeather(CommandContext<CommandSourceStack> commandSourceStackCommandContext)
    {
        BritishWeather.getWeatherManager().setWeather(commandSourceStackCommandContext.getSource().getServer());
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.translatable("br.command.refresh.success"), false);

        return 0;
    }

    private static int getWeather(CommandContext<CommandSourceStack> cmd)
    {
        ServerWeatherManager.WeatherState weatherState = BritishWeather.getWeatherManager().getState();
        String weatherStr = weatherState.getWeatherCondition().getDisplayName();
        WeatherLocationSource.Location location = weatherState.getLocation();

        // Check if location should be shown. We don't want people getting doxxed
        Component message = BritishWeather.getConfig().shouldShowLocationToClients() && location != null
                ? Component.translatable("br.command.brweather.message", weatherStr, location.name())
                : Component.translatable("br.command.brweather.message_no_location", weatherStr);

        cmd.getSource().sendSuccess(() -> message, false);
        return 0; // What does this return value do?
    }
}
