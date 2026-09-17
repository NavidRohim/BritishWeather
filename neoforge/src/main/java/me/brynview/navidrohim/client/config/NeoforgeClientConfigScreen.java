package me.brynview.navidrohim.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import me.brynview.navidrohim.common.config.NeoforgeConfigSerializableValues;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class NeoforgeClientConfigScreen
{
    private record PostcodeStringController(Option<String> option) implements IStringController<String>
    {
        @Override
        public String getString()
        {
            return option.pendingValue();
        }

        @Override
        public void setFromString(String value)
        {
            option.requestSet(value);
        }

        @Override
        public Option<String> option()
        {
            return option;
        }

        @Override
        public boolean isInputValid(String input)
        {
            return input.length() <= 7 && !input.contains(" ");
        }

        public static class PostcodeStringControllerBuilderImpl extends AbstractControllerBuilderImpl<String> implements ControllerBuilder<String>
        {
            protected PostcodeStringControllerBuilderImpl(Option<String> option)
            {
                super(option);
            }

            public Controller<String> build()
            {
                return new PostcodeStringController(option);
            }
        }
    }

    public static Screen getModConfigScreenFactory(Screen parent)
    {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("br.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("br.config.category.weather"))

                        .option(Option.<Float>createBuilder() // Lat (float)
                                .name(Component.translatable("br.config.category.weather.lat"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.lat.description")))
                                .binding(NeoforgeConfigSerializableValues.lat,
                                        () -> NeoforgeConfigSerializableValues.lat,
                                        newVal -> NeoforgeConfigSerializableValues.lat = Math.clamp(newVal, -90, 90)) // Make sure it's an actual valid latitude value (-90 - 90)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Float>createBuilder() // Lon (float)
                                .name(Component.translatable("br.config.category.weather.long"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.long.description")))
                                .binding(NeoforgeConfigSerializableValues.lon,
                                        () -> NeoforgeConfigSerializableValues.lon,
                                        newLonVal -> NeoforgeConfigSerializableValues.lon = Math.clamp(newLonVal, -180, 180)) // Make sure it's an actual longitude value (-180 - 180, does not use 360deg format)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<String>createBuilder()
                                .name(Component.translatable("br.config.category.weather.postcode"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.postcode.description")))
                                .binding(NeoforgeConfigSerializableValues.postcode,
                                        () -> NeoforgeConfigSerializableValues.postcode,
                                        newPostcodeVal -> NeoforgeConfigSerializableValues.postcode = newPostcodeVal
                                )
                                .controller(PostcodeStringController.PostcodeStringControllerBuilderImpl::new)
                                .build()

                        ).option(Option.<Integer>createBuilder() // Weather fetch interval, uncapped integer
                                .name(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs.description")))
                                .binding(NeoforgeConfigSerializableValues.weatherStatusRefreshIntervalTicks,
                                        () -> NeoforgeConfigSerializableValues.weatherStatusRefreshIntervalTicks / 20,
                                        newDurationVal -> NeoforgeConfigSerializableValues.weatherStatusRefreshIntervalTicks = newDurationVal * 20)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<String>createBuilder()
                                .name(Component.translatable("br.config.category.weather.locationMethod"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.locationMethod.description")))
                                .binding(NeoforgeConfigSerializableValues.locationOptions, () -> NeoforgeConfigSerializableValues.locationOptions, (val) -> NeoforgeConfigSerializableValues.locationOptions = val)
                                .controller(option -> CyclingListControllerBuilder.create(option)
                                        .values(WeatherLocationSources.getSources())
                                        .formatValue(string -> Component.literal(string.toUpperCase())))
                                .build()

                        ).build()

                ).category(ConfigCategory.createBuilder()
                        .name(Component.translatable("br.config.category.admin"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("br.config.category.weather.shouldShowLocation"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.shouldShowLocation.description")))
                                .binding(NeoforgeConfigSerializableValues.shouldShowLocationToClients, () -> NeoforgeConfigSerializableValues.shouldShowLocationToClients, (val) -> NeoforgeConfigSerializableValues.shouldShowLocationToClients = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("br.config.category.admin.hailShouldDealDamage"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.admin.hailShouldDealDamage.description")))
                                .binding(NeoforgeConfigSerializableValues.hailShouldDealDamage, () -> NeoforgeConfigSerializableValues.hailShouldDealDamage, (val) -> NeoforgeConfigSerializableValues.hailShouldDealDamage = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("br.config.category.admin.debug"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.admin.debug.description")))
                                .binding(NeoforgeConfigSerializableValues.debug, () -> NeoforgeConfigSerializableValues.debug, (bool) -> NeoforgeConfigSerializableValues.debug = bool)
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        )
                        .build()
                )

                .save(() -> NeoforgeConfigSerializableValues.HANDLER.save()) // Save config to file everytime save button is pressed.
                .build()
                .generateScreen(parent); // Go to ModMenu screen when finished*/
    }
}
