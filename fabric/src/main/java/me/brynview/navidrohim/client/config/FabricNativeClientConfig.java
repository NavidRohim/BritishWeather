package me.brynview.navidrohim.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.brynview.navidrohim.common.config.FabricCommonSideConfig;
import net.minecraft.network.chat.Component;

public class FabricNativeClientConfig implements ModMenuApi
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

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {

        // I always hate how configs are set up. It works great, but looks so ugly here.
        // Also, does Component.translatable() have to be called every time, for every key, every time the config screen is rendered? Seems wasteful.
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("br.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("br.config.category.weather"))

                        .option(Option.<Float>createBuilder() // Lat (float)
                                .name(Component.translatable("br.config.category.weather.lat"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.lat.description")))
                                .binding(FabricCommonSideConfig.lat,
                                        () -> FabricCommonSideConfig.lat,
                                        newVal -> FabricCommonSideConfig.lat = Math.clamp(newVal, -90, 90)) // Make sure it's an actual valid latitude value (-90 - 90)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Float>createBuilder() // Lon (float)
                                .name(Component.translatable("br.config.category.weather.long"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.long.description")))
                                .binding(FabricCommonSideConfig.lon,
                                        () -> FabricCommonSideConfig.lon,
                                        newLonVal -> FabricCommonSideConfig.lon = Math.clamp(newLonVal, -180, 180)) // Make sure it's an actual longitude value (-180 - 180, does not use 360deg format)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<String>createBuilder()
                                .name(Component.translatable("br.config.category.weather.postcode"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.postcode.description")))
                                .binding(FabricCommonSideConfig.postcode,
                                        () -> FabricCommonSideConfig.postcode,
                                        newPostcodeVal -> FabricCommonSideConfig.postcode = newPostcodeVal
                                )
                                .controller(PostcodeStringController.PostcodeStringControllerBuilderImpl::new)
                                .build()

                        ).option(Option.<Integer>createBuilder() // Weather fetch interval, uncapped integer
                                .name(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs.description")))
                                .binding(FabricCommonSideConfig.weatherStatusRefreshIntervalTicks,
                                        () -> FabricCommonSideConfig.weatherStatusRefreshIntervalTicks / 20,
                                        newDurationVal -> FabricCommonSideConfig.weatherStatusRefreshIntervalTicks = newDurationVal * 20)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<FabricCommonSideConfig.FabricLocationOptions>createBuilder()
                                .name(Component.translatable("br.config.category.weather.locationMethod"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.locationMethod.description")))
                                .binding(Binding.generic(FabricCommonSideConfig.locationOptions, () -> FabricCommonSideConfig.locationOptions, (val) -> {
                                    FabricCommonSideConfig.locationOptions = val;
                                }))
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(FabricCommonSideConfig.FabricLocationOptions.class))
                                .build()

                    ).build()
                )

                .save(() -> FabricCommonSideConfig.HANDLER.save()) // Save config to file everytime save button is pressed.
                .build()
                .generateScreen(parent); // Go to ModMenu screen when finished
    }
}
