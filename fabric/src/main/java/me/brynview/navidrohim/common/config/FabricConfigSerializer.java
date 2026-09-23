package me.brynview.navidrohim.common.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.brynview.navidrohim.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

/*
Again, separate from FabricClientConfigScreen and another classes to avoid classloading
 */
public class FabricConfigSerializer
{

    public static ConfigClassHandler<FabricConfigSerializer> HANDLER = ConfigClassHandler.createBuilder(FabricConfigSerializer.class)
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
    public static String locationOptions = Constants.DefaultConfigValues.defaultLocationMethod.getIdentifier();

    @SerialEntry
    public static boolean shouldShowLocationToClients = Constants.DefaultConfigValues.shouldShowLocationToClients;
    @SerialEntry
    public static boolean hailShouldDealDamage = Constants.DefaultConfigValues.hailShouldDealDamage;
    @SerialEntry
    public static boolean debug = Constants.DefaultConfigValues.debug;

    @SerialEntry
    public static boolean shouldRenderCompass = Constants.DefaultConfigValues.shouldRenderCompass;
    @SerialEntry
    public static boolean shouldRenderHeading = Constants.DefaultConfigValues.shouldRenderHeading;
    @SerialEntry
    public static int compassSize = Constants.DefaultConfigValues.compassSize;
    @SerialEntry
    public static int compassY = Constants.DefaultConfigValues.compassY;

}
