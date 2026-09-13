package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonMLConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FabricCommonMLConfig implements CommonMLConfig
{

    @Override
    public Float getLongitude()
    {
        return FabricCommonSideConfig.lon;
    }

    @Override
    public Float getLatitude()
    {
        return FabricCommonSideConfig.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return FabricCommonSideConfig.weatherStatusRefreshIntervalTicks; // Convert to ticks
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        @Nullable FabricCommonSideConfig.FabricLocationOptions locationOption = FabricCommonSideConfig.locationOptions;
        if (locationOption != null)
        {
            return WeatherLocationSources.getSource(FabricCommonSideConfig.locationOptions.getKey());
        }
        throw new RuntimeException("locationOptions has an invalid value. Can only be " + Arrays.toString(FabricCommonSideConfig.FabricLocationOptions.values()));
    }

    @Override
    public String getPostcode()
    {
        return FabricCommonSideConfig.postcode;
    }
}
