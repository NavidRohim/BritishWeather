package me.brynview.navidrohim.common;

import me.brynview.navidrohim.Constants;

import java.util.List;
import java.util.function.Consumer;

public class ConfigTest
{
    public static final class Values
    {
        public static Boolean debug = Constants.DefaultConfigValues.debug;
    }

    private static final List<ConfigValue> valuesForWeather = List.of
    (
    );
    private static final List<ConfigValue> valuesForAdmin = List.of
    (
            new ConfigValue<>("debug", Boolean.class,
                    new ConfigValue.CommonBinder(Constants.DefaultConfigValues.debug, () -> Values.debug, (n) -> Values.debug = (Boolean) n))
    );
    public static final List<ConfigCategory> categoriesWithValues = List.of
    (
        new ConfigCategory("weather", valuesForWeather),
        new ConfigCategory("admin", valuesForAdmin)
    );
}
