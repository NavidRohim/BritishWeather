package me.brynview.navidrohim.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import me.brynview.navidrohim.common.config.FabricConfigSerializer;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/*
This class is separate to avoid classloading yacl classes if it's not installed.
Which of course, will crash the game
 */
public class FabricClientConfigScreen
{
    public static Screen getConfigScreen(Screen parent)
    {
        // I always hate how configs are set up. It works great, but looks so ugly here.
        // Also, does Component.translatable() have to be called every time, for every key, every time the config screen is rendered? Seems wasteful.
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("br.config.title"))

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("br.config.category.weather"))
                        .option(Option.<Float>createBuilder() // Lat (float)
                                .name(Component.translatable("br.config.category.weather.lat"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.lat.description")))
                                .binding(FabricConfigSerializer.lat,
                                        () -> FabricConfigSerializer.lat,
                                        newVal -> FabricConfigSerializer.lat = Math.clamp(newVal, -90, 90)) // Make sure it's an actual valid latitude value (-90 - 90)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Float>createBuilder() // Lon (float)
                                .name(Component.translatable("br.config.category.weather.long"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.long.description")))
                                .binding(FabricConfigSerializer.lon,
                                        () -> FabricConfigSerializer.lon,
                                        newLonVal -> FabricConfigSerializer.lon = Math.clamp(newLonVal, -180, 180)) // Make sure it's an actual longitude value (-180 - 180, does not use 360deg format)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<String>createBuilder()
                                .name(Component.translatable("br.config.category.weather.postcode"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.postcode.description")))
                                .binding(FabricConfigSerializer.postcode,
                                        () -> FabricConfigSerializer.postcode,
                                        newPostcodeVal -> FabricConfigSerializer.postcode = newPostcodeVal
                                )
                                .controller(PostcodeStringController.PostcodeStringControllerBuilderImpl::new)
                                .build()

                        ).option(Option.<Integer>createBuilder() // Weather fetch interval, uncapped integer
                                .name(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs.description")))
                                .binding(FabricConfigSerializer.weatherStatusRefreshIntervalTicks,
                                        () -> FabricConfigSerializer.weatherStatusRefreshIntervalTicks / 20,
                                        newDurationVal -> FabricConfigSerializer.weatherStatusRefreshIntervalTicks = newDurationVal * 20)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<String>createBuilder()
                                .name(Component.translatable("br.config.category.weather.locationMethod"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.locationMethod.description")))
                                .binding(FabricConfigSerializer.locationOptions, () -> FabricConfigSerializer.locationOptions, (val) -> FabricConfigSerializer.locationOptions = val)
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
                                .binding(FabricConfigSerializer.shouldShowLocationToClients, () -> FabricConfigSerializer.shouldShowLocationToClients, (val) -> FabricConfigSerializer.shouldShowLocationToClients = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("br.config.category.admin.hailShouldDealDamage"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.admin.hailShouldDealDamage.description")))
                                .binding(FabricConfigSerializer.hailShouldDealDamage, () -> FabricConfigSerializer.hailShouldDealDamage, (val) -> FabricConfigSerializer.hailShouldDealDamage = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("br.config.category.admin.debug"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.admin.debug.description")))
                                .binding(FabricConfigSerializer.debug, () -> FabricConfigSerializer.debug, (bool) -> FabricConfigSerializer.debug = bool)
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        )
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("br.config.category.compass"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("br.config.category.compass.enabled"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.compass.enabled.description")))
                                .binding(FabricConfigSerializer.shouldRenderCompass, () -> FabricConfigSerializer.shouldRenderCompass, (bool) -> FabricConfigSerializer.shouldRenderCompass = bool)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("br.config.category.compass.size"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.compass.size.description")))
                                .binding(FabricConfigSerializer.compassSize, () -> FabricConfigSerializer.compassSize, (val) -> FabricConfigSerializer.compassSize = val)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(1, 100)
                                        .step(1)
                                        .formatValue(val -> Component.literal(val + "%")))
                                .build())
                        .build())

                .save(() -> FabricConfigSerializer.HANDLER.save()) // Save config to file everytime save button is pressed.
                .build()
                .generateScreen(parent); // Go to ModMenu screen when finished
    }

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
}
