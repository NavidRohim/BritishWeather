package me.brynview.navidrohim.common.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FabricNativeModConfig implements ModMenuApi
{
    private static final String modConfigFile = "%s.json5".formatted(Constants.MOD_ID);

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

    public enum LocationOptions implements NameableEnum
    {

        IP_GEOLOCATION("ip"),
        POSTCODE_GEOLOCATION("postcode"),
        MANUAL_GEOLOCATION("manual");

        final String key;

        LocationOptions(String key)
        {
            this.key = key;
        }

        @Override
        public Component getDisplayName()
        {
            return net.minecraft.network.chat.Component.translatable("br.config.category.weather.%s".formatted(key));
        }
    }

    public static ConfigClassHandler<FabricNativeModConfig> HANDLER = ConfigClassHandler.createBuilder(FabricNativeModConfig.class)
            .id(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config"))
            .serializer(modConfigConfigClassHandler -> GsonConfigSerializerBuilder.create(modConfigConfigClassHandler)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(modConfigFile))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public static float lat = 51.895776F; // Default lat

    @SerialEntry
    public static float lon = -3.049397F; // Default lon

    @SerialEntry
    public static int fetchWeatherStatusIntervalInSeconds = 120; // Fetch every 2 minutes by default

    @SerialEntry
    public static String postcode = "NP77LP"; // Default postcode

    @SerialEntry
    public static LocationOptions locationOptions = LocationOptions.IP_GEOLOCATION;

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
                                        () -> fetchWeatherStatusIntervalInSeconds,
                                        newDurationVal -> fetchWeatherStatusIntervalInSeconds = newDurationVal)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<LocationOptions>createBuilder()
                                .name(Component.translatable("br.config.category.weather.locationMethod"))
                                .description(OptionDescription.of(Component.translatable("br.config.category.weather.locationMethod.description")))
                                .binding(Binding.generic(locationOptions, () -> locationOptions, (val) -> {
                                    locationOptions = val;
                                }))
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(LocationOptions.class))
                                .build()

                    ).build()
                )

                .save(() -> HANDLER.save()) // Save config to file everytime save button is pressed.
                .build()
                .generateScreen(parent); // Go to ModMenu screen when finished
    }
}
