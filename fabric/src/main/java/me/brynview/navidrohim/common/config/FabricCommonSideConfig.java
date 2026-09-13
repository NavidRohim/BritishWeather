package me.brynview.navidrohim.common.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.config.LocationOptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FabricCommonSideConfig
{

    public static ConfigClassHandler<FabricCommonSideConfig> HANDLER = ConfigClassHandler.createBuilder(FabricCommonSideConfig.class)
            .id(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config"))
            .serializer(modConfigConfigClassHandler -> GsonConfigSerializerBuilder.create(modConfigConfigClassHandler)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(Constants.DefaultConfigValues.modConfigFile))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public static float lat = Constants.DefaultConfigValues.latitude; // Default lat
    @SerialEntry
    public static float lon = Constants.DefaultConfigValues.longitude; // Default lon
    @SerialEntry
    public static int weatherStatusRefreshIntervalTicks = Constants.DefaultConfigValues.weatherRefreshInterval; // Fetch every 3 minutes by default
    @SerialEntry
    public static String postcode = Constants.DefaultConfigValues.postcode; // Default postcode
    @SerialEntry
    public static FabricLocationOptions locationOptions = FabricLocationOptions.valueOf(Constants.DefaultConfigValues.locationOption);

    public enum FabricLocationOptions implements NameableEnum, LocationOptions
    {

        IP,
        POSTCODE,
        MANUAL;

        final String key;

        FabricLocationOptions()
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
            return key;
        }
    }
}
