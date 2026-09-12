package me.brynview.navidrohim.server.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.platform.NeoForgePlatformHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Path;

public class NeoforgeNativeModConfig
{
    public static ConfigClassHandler<NeoforgeNativeModConfig> HANDLER = ConfigClassHandler.createBuilder(NeoforgeNativeModConfig.class)
            .id(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config"))
            .serializer(modConfigConfigClassHandler -> GsonConfigSerializerBuilder.create(modConfigConfigClassHandler)
                    .setPath(FMLPaths.CONFIGDIR.get().resolve(Constants.DefaultConfigValues.modConfigFile))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

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

    public enum NeoforgeLocationOptions implements NameableEnum, LocationOptions
    {

        IP,
        MANUAL,
        POSTCODE;

        private final String key;
        NeoforgeLocationOptions()
        {
            this.key = this.name().toLowerCase();
        }
        @Override
        public Component getDisplayName()
        {
            return Component.translatable("br.config.category.weather.%s".formatted(getKey()));
        }

        @Override
        public String getKey()
        {
            return this.key;
        }
    }

    @SerialEntry
    public static float lat = Constants.DefaultConfigValues.latitude; // Default lat

    @SerialEntry
    public static float lon = Constants.DefaultConfigValues.longitude; // Default lon

    @SerialEntry
    public static int fetchWeatherStatusIntervalInSeconds = Constants.DefaultConfigValues.weatherRefreshInterval; // Fetch every 3 minutes by default

    @SerialEntry
    public static String postcode = Constants.DefaultConfigValues.postcode; // Default postcode

    @SerialEntry
    public static NeoforgeLocationOptions locationOptions = NeoforgeLocationOptions.valueOf(Constants.DefaultConfigValues.locationOption);

    public static Screen getModConfigScreenFactory(Screen parent)
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
                                .binding(lat,
                                        () -> lat,
                                        newVal -> lat = Math.clamp(newVal, -90, 90)) // Make sure it's an actual valid latitude value (-90 - 90)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Float>createBuilder() // Lon (float)
                                .name(Component.translatable("br.config.category.weather.long"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.long.description")))
                                .binding(lon,
                                        () -> lon,
                                        newLonVal -> lon = Math.clamp(newLonVal, -180, 180)) // Make sure it's an actual longitude value (-180 - 180, does not use 360deg format)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<String>createBuilder()
                                .name(Component.translatable("br.config.category.weather.postcode"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.postcode.description")))
                                .binding(postcode,
                                        () -> postcode,
                                        newPostcodeVal -> postcode = newPostcodeVal
                                )
                                .controller(PostcodeStringController.PostcodeStringControllerBuilderImpl::new)
                                .build()

                        ).option(Option.<Integer>createBuilder() // Weather fetch interval, uncapped integer
                                .name(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs.description")))
                                .binding(fetchWeatherStatusIntervalInSeconds,
                                        () -> fetchWeatherStatusIntervalInSeconds / 20,
                                        newDurationVal -> fetchWeatherStatusIntervalInSeconds = newDurationVal * 20)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<NeoforgeLocationOptions>createBuilder()
                                .name(Component.translatable("br.config.category.weather.locationMethod"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.locationMethod.description")))
                                .binding(Binding.generic(locationOptions, () -> locationOptions, (val) -> {
                                    locationOptions = val;
                                }))
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(NeoforgeLocationOptions.class))
                                .build()

                        ).build()
                )

                .save(() -> HANDLER.save()) // Save config to file everytime save button is pressed.
                .build()
                .generateScreen(parent); // Go to ModMenu screen when finished
    }
}
