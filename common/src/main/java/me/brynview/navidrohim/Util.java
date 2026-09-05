package me.brynview.navidrohim;

import me.brynview.navidrohim.server.ServerWeatherManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class Util
{
    @Nullable
    public static String getIP()
    {
        try
        {
            // Not the best. If there is an error, it is not handled and assumed to be an IP.
            URL IPUrl = URI.create(Constants.IP_CHECK_URL).toURL();
            try (InputStream stream = IPUrl.openStream())
            {
                return new String(stream.readAllBytes()).strip();
            }
        } catch (IOException e)
        {
            Constants.LOG.error("Couldn't get IP from {}, try use coordinates instead.", Constants.IP_CHECK_URL);
            return null;
        }
    }

    public static URI getWeatherAPIUrl()
    {
        // https://open-meteo.com/en/docs to determine endpoint
        return URI.create(String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=weather_code&timezone=auto", ServerWeatherManager.getLatFromIP(), ServerWeatherManager.getLonFromIP()));
    }
}
