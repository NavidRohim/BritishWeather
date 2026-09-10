package me.brynview.navidrohim.server.weather.sources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.weather.WeatherCache;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static me.brynview.navidrohim.server.weather.ServerWeatherManager.HTTP_CLIENT;

public final class PostcodeLocationSource implements WeatherLocationSource
{

    private static final String KEY = "postcode";
    private static WeatherCache CACHE;

    @Override
    public @Nullable WeatherCache getCache()
    {
        return CACHE;
    }

    @Override
    public void setCache(WeatherCache cache)
    {
        CACHE = cache;
    }

    @Override
    public boolean shouldHaveCache()
    {
        return true;
    }

    @Override
    public String getKey()
    {
        return KEY;
    }

    private URI getPostcodesIOEndpointForPostcode(String postcode)
    {
        // Docs for endpoint: https://postcodes.io/docs/
        return URI.create("https://api.postcodes.io/postcodes/?query=%s&filter=latitude,longitude,admin_ward".formatted(postcode));
    }

    @Override
    public void getLocation(TriConsumer<Location, WeatherLocationSource, String> callable)
    {
        String postcode = BritishWeather.getConfig().getPostcode();
        if (getCache().isNotCached(postcode))
        {
            Constants.LOG.info("Cache miss on postcode. {} != {}", postcode, getCache().getKey());
            URI endpoint = getPostcodesIOEndpointForPostcode(postcode);
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"postcodes\": [\"%s\"]}".formatted(BritishWeather.getConfig().getPostcode())))
                    .header("Content-Type", "application/json")
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse ->
            {
                int statusCode = stringHttpResponse.statusCode();
                JsonObject returnedObj = JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject();

                if (statusCode == 200)
                {
                    try
                    {
                        JsonObject jsonObjectResult = returnedObj // tf is this?
                                .getAsJsonArray("result")
                                .get(0)
                                .getAsJsonObject()
                                .getAsJsonObject("result");

                        float lat = jsonObjectResult.get("latitude").getAsFloat();
                        float lon = jsonObjectResult.get("longitude").getAsFloat();
                        String parish = jsonObjectResult.get("admin_ward").getAsString();

                        callable.accept(new Location(lat, lon, parish), this, postcode);
                    } catch (ClassCastException e)
                    {
                        Constants.LOG.warn("Could not parse response. This is usually because the postcode value in the config is wrong. Postcode must be full length. For example: NP77LP, not just NP77.");
                    } catch (Exception e)
                    {
                        Constants.LOG.error("Could not get lat/long for postcode {} Error {} Body {}", endpoint, e.getMessage(), returnedObj.toString());
                    }
                } else if (statusCode == 404)
                {
                    Constants.LOG.error("Postcode not found {}", postcode);
                } else if (statusCode == 500)
                {
                    Constants.LOG.error("API endpoint is down for postcode location method. Try manual latitude/longitude or IP");
                }
            });
        } else {
            Constants.LOG.info("Cache hit on postcode.");
            callable.accept(getCache().getCachedWeatherState().getLocation(), this,  postcode);
        }
    }
}
