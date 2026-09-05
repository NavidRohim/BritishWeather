package me.brynview.navidrohim.server.locationsource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static me.brynview.navidrohim.server.ServerWeatherManager.HTTP_CLIENT;

public class IPLocationSource implements LocationSource
{

    @Override
    public void getLocation(BiConsumer<Location, LocationSource> callable)
    {
        if (!CommonClass.getCache().isIpCached())
        {
            HttpRequest requestIPApi = HttpRequest.newBuilder(Constants.IP_API_ENDPOINT).GET().build();

            // Send request asynchronously. Check status code is valid and API request is successful. View docs here
            // https://ip-api.com/docs/api:json
            HTTP_CLIENT.sendAsync(requestIPApi, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> {

                int statusCode =            stringHttpResponse.statusCode();
                JsonObject returnedObj =    JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject();
                // TODO error handling
                if (statusCode == 200 && returnedObj.get("status").getAsString().equals("success"))
                {
                    float lat_ip =        returnedObj.get("lat").getAsFloat();
                    float lon_ip =        returnedObj.get("lon").getAsFloat();
                    String area =         returnedObj.get("city").getAsString() + returnedObj.get("district").getAsString(); // "district" may be irrelevant in the UK?
                    Location location = new Location(lat_ip, lon_ip, area);

                    Constants.LOG.info("Getting location information via IP");
                    callable.accept(location, this);
                }
            });
        } else {
            if (CommonClass.getWeatherManager().getState().isPresent())
            {
                Constants.LOG.info("Getting location information via IP cache.");
                callable.accept(CommonClass.getWeatherManager().getState().get().getLocation(), this);
            } else if (CommonClass.getCache().getCachedWeatherState() != null)
            {
                Constants.LOG.warn("Using cached weather state when getState() is not present! This is very bad!");
                callable.accept(CommonClass.getCache().getCachedWeatherState().getLocation(), this);
            } else {
                throw new RuntimeException("No valid weather state found for IP!");
            }

        }
    }
}
