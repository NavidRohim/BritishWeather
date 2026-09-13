package me.brynview.navidrohim.common.config;

import me.brynview.navidrohim.server.config.CommonMLConfig;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NeoforgeCommonMLConfig implements CommonMLConfig
{
    @Override
    public Float getLongitude()
    {
        return NeoforgeCommonSideConfig.lon;
    }

    @Override
    public Float getLatitude()
    {
        return NeoforgeCommonSideConfig.lat;
    }

    @Override
    public Integer getWeatherFetchIntervalInTicks()
    {
        return NeoforgeCommonSideConfig.weatherStatusRefreshIntervalTicks;
    }

    @Override
    public WeatherLocationSource getLocationSource()
    {
        @Nullable NeoforgeCommonSideConfig.NeoforgeLocationOptions locationOptions = NeoforgeCommonSideConfig.locationOptions;
        if (locationOptions != null)
        {
            return WeatherLocationSources.getSource(NeoforgeCommonSideConfig.locationOptions.getKey());
        }
        throw new RuntimeException("locationOptions has an invalid value. Can only be " + Arrays.toString(NeoforgeCommonSideConfig.NeoforgeLocationOptions.values()));
    }

    @Override
    public String getPostcode()
    {
        return NeoforgeCommonSideConfig.postcode;
    }
}
