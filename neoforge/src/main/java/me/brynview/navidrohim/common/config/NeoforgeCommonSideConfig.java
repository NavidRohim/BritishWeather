package me.brynview.navidrohim.common.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.brynview.navidrohim.Constants;
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
    public static String locationOptions = Constants.DefaultConfigValues.defaultLocationMethod.getIdentifier();
    @SerialEntry
    public static boolean shouldShowLocationToClients = Constants.DefaultConfigValues.shouldShowLocationToClients;


}
