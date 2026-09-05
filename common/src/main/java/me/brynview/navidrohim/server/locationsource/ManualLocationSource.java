package me.brynview.navidrohim.server.locationsource;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ManualLocationSource implements LocationSource
{
    @Override
    public void getLocation(BiConsumer<Location, LocationSource> callable)
    {
        Constants.LOG.info("Getting location information via Lat/Long manual");
        callable.accept(new Location(CommonClass.getConfig().getLatitude(), CommonClass.getConfig().getLongitude(), "Earth"), this);
    }
}
