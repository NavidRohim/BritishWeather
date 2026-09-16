package me.brynview.navidrohim.common;

import java.util.List;

public record ConfigCategory(String categoryName, List<ConfigValue> values)
{
    public ConfigCategory(String categoryName, List<ConfigValue> values)
    {
        this.categoryName = categoryName;
        this.values = values;

        for (ConfigValue value : values)
        {
            value.setOwner(this);
        }
    }
}
