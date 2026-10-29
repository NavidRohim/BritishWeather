package me.brynview.navidrohim.client.integration;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;
import me.brynview.navidrohim.Constants;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneyMapFabric implements IClientPlugin
{
    private static JourneyMapFabric INSTANCE;
    private IClientAPI api;

    public JourneyMapFabric()
    {
        INSTANCE = this;
    }

    public static JourneyMapFabric getInstance()
    {
        return INSTANCE;
    }

    public IClientAPI getApi()
    {
        return api;
    }

    @Override
    public String getModId()
    {
        return Constants.MOD_ID;
    }

    @Override
    public void initialize(final IClientAPI jmClientApi)
    {
        api = jmClientApi;
    }


}
