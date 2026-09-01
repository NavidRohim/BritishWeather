package me.brynview.navidrohim.config;

import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import me.brynview.navidrohim.Constants;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FabricNativeModConfig implements ModMenuApi
{
    private static final String modConfigFile = "%s.json5".formatted(Constants.MOD_ID);

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
    public static boolean usePlayerIP = true;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {

        // I always hate how configs are set up. It works great, but looks so ugly here.
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("br.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("br.config.category.weather"))

                        .option(Option.<Float>createBuilder() // Lat (float)
                                .name(Component.translatable("br.config.category.weather.lat"))
                                .binding(lat,
                                        () -> lat,
                                        newVal -> lat = Math.clamp(newVal, -90, 90)) // Make sure it's an actual valid latitude value (-90 - 90)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Float>createBuilder() // Lon (float)
                                .name(Component.translatable("br.config.category.weather.long"))
                                .binding(lon,
                                        () -> lon,
                                        newLonVal -> lon = Math.clamp(newLonVal, -180, 180)) // Make sure it's an actual longitude value (-180 - 180, does not use 360deg format)
                                .controller(FloatFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Integer>createBuilder() // Weather fetch interval, uncapped integer
                                .name(Component.translatable("br.config.category.weather.fetchWeatherIntervalSecs"))
                                .binding(fetchWeatherStatusIntervalInSeconds,
                                        () -> fetchWeatherStatusIntervalInSeconds,
                                        newDurationVal -> fetchWeatherStatusIntervalInSeconds = newDurationVal)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build()

                        ).option(Option.<Boolean>createBuilder() // Use player IP to geolocate, boolean
                                .name(Component.translatable("br.config.category.weather.useIp"))
                                .binding(usePlayerIP,
                                        () -> usePlayerIP,
                                        newVal -> usePlayerIP = newVal)
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        ).build()
                )
                .save(() -> HANDLER.save()) // Save config to file everytime save button is pressed.
                .build()
                .generateScreen(parent); // Go to ModMenu screen when finished
    }
}
