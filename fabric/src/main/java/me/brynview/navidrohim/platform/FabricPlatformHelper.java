package me.brynview.navidrohim.platform;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.integration.JourneyMapFabric;
import me.brynview.navidrohim.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public @Nullable IClientAPI getJourneyMapIntegration()
    {
        return JourneyMapFabric.getInstance().getApi();
    }
}
