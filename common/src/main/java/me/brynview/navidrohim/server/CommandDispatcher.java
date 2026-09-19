package me.brynview.navidrohim.server;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.common.WeatherCondition;
import me.brynview.navidrohim.server.weather.ServerWeatherManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.Arrays;

public class CommandDispatcher
{
    public static void registerCommandsForDispatcher(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("brweather")
                        .then(Commands.literal("change").requires((c) -> !c.isPlayer() || c.getPlayer().permissions().hasPermission(Permissions.COMMANDS_ADMIN)).then(Commands.argument("weather_condition", StringArgumentType.string()).executes(CommandDispatcher::getWeather)))
                        .then(Commands.literal("get").executes(CommandDispatcher::changeWeather)));
    }

    private static int changeWeather(CommandContext<CommandSourceStack> commandSourceStackCommandContext)
    {
        if (!BritishWeather.getConfig().debug())
        {
            return 1;
        }

        String weatherType = commandSourceStackCommandContext.getArgument("weather_condition", String.class);
        try
        {
            {
                WeatherCondition weatherCondition = WeatherCondition.valueOf(weatherType);
                BritishWeather.getWeatherManager().setState(new ServerWeatherManager.WeatherState(0, weatherCondition, BritishWeather.getWeatherManager().getState().getLocation()));
                BritishWeather.getWeatherManager().changeServerWeather(commandSourceStackCommandContext.getSource().getServer(), true);
                commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Changed weather to %s".formatted(weatherCondition.toString())), false);

            }
        } catch (IllegalArgumentException e)
        {
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal("Invalid weather condition. Options are %s".formatted(Arrays.stream(WeatherCondition.values()).toList())));
        }

        return 0;
    }

    private static int getWeather(CommandContext<CommandSourceStack> cmd)
    {
        ServerWeatherManager.WeatherState weatherState = BritishWeather.getWeatherManager().getState();
        String weatherStr = weatherState.getWeatherCondition().getDisplayName();
        String location = weatherState.getLocation().name();

        // Check if location should be shown. We don't want people getting doxxed
        Component message = BritishWeather.getConfig().shouldShowLocationToClients()
                ? Component.translatable("br.command.brweather.message", weatherStr, location)
                : Component.translatable("br.command.brweather.message_no_location", weatherStr);

        cmd.getSource().sendSuccess(() -> message, false);
        return 0; // What does this return value do?
    }
}
