package me.brynview.navidrohim.server.locationsource;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface LocationSource
{
    record Location(float lat, float lon, String name) {}

    void getLocation(BiConsumer<Location, LocationSource> callable);
}
