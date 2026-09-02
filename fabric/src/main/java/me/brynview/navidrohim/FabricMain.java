package me.brynview.navidrohim;

import me.brynview.navidrohim.config.FabricCommonModConfig;
import me.brynview.navidrohim.config.FabricNativeModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {

        // init
        FabricNativeModConfig.HANDLER.load();
        CommonClass.init(new FabricCommonModConfig());

        // events
        ServerTickEvents.END_SERVER_TICK.register(CommonClass::onTick);
    }
}
