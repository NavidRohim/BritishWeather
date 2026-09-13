package me.brynview.navidrohim.common.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.config.LocationOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;

public class NeoforgeCommonSideConfig
{
    public static ConfigClassHandler<NeoforgeCommonSideConfig> HANDLER = ConfigClassHandler.createBuilder(NeoforgeCommonSideConfig.class)
            .id(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config"))
            .serializer(modConfigConfigClassHandler -> GsonConfigSerializerBuilder.create(modConfigConfigClassHandler)
                    .setPath(FMLPaths.CONFIGDIR.get().resolve(Constants.DefaultConfigValues.modConfigFile))
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
    public static NeoforgeLocationOptions locationOptions = NeoforgeLocationOptions.valueOf(Constants.DefaultConfigValues.locationOption);

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
}
