package me.brynview.navidrohim.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.brynview.navidrohim.FabricMain;
import me.brynview.navidrohim.client.screen.NoConfigScreen;

public class FabricClientConfig implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return !FabricMain.hasYacl ? NoConfigScreen::fromScreen : FabricClientConfigScreen::getConfigScreen;
    }
}
