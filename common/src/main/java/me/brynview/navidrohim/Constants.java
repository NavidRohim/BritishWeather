package me.brynview.navidrohim;

import me.brynview.navidrohim.server.weather.sources.WeatherLocationSource;
import me.brynview.navidrohim.server.weather.sources.WeatherLocationSources;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class Constants {

    public static final String MOD_ID = "britishweather";
    public static final String MOD_NAME = "British Weather";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static final String IP_CHECK_URL = "https://checkip.amazonaws.com";
    public static final @Nullable String USER_IP = Util.getIP();
    public static final URI IP_API_ENDPOINT = URI.create("http://ip-api.com/json/%s?fields=573648".formatted(Constants.USER_IP));

    public static final String DEFAULT_LOCATION_NAME = "Earth";

    public static final class DefaultConfigValues
    {
        public static final String modConfigFile = "%s.json5".formatted(Constants.MOD_ID);
        public static final WeatherLocationSource defaultLocationMethod = WeatherLocationSources.IP;

        public static final float latitude = 51.895776F;
        public static final float longitude = -3.049397F;
        public static final int weatherRefreshInterval = 3600;
        public static final String postcode = "NP77LP";
        public static final String locationOption = defaultLocationMethod.getIdentifier();
        public static final boolean shouldShowLocationToClients = false;
    }
}