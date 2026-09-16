package me.brynview.navidrohim.common;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigValue<T>
{

    public static class CommonBinder<T>
    {
        private final T def;
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        public CommonBinder(T def, Supplier<T> getter, Consumer<T> setter)
        {
            this.def = def;
            this.getter = getter;
            this.setter = setter;
        }

        public T get()
        {
            return getter.get();
        }

        public void set(T binderValue)
        {
            setter.accept(binderValue);
        }

        public T getDefault()
        {
            return def;
        }
    }

    public String key;
    public Class<T> type;
    public CommonBinder binder;

    public ConfigCategory owner;

    public ConfigValue(String key, Class<T> type, CommonBinder binder)
    {
        this.key = key;
        this.type = type;
        this.binder = binder;
    }

    public void setOwner(ConfigCategory owner)
    {
        if (this.owner != null)
        {
            throw new IllegalStateException("Cannot set owner twice");
        }
        this.owner = owner;
    }

    public String getKey()
    {
        return key;
    }
}
