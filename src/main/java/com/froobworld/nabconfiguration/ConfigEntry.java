package com.froobworld.nabconfiguration;

import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigEntry<T> implements Supplier<T> {
    private final Function<Object, T> mappingFunction;
    private T value;

    public ConfigEntry() {
        this(object -> (T) object);
    }

    public ConfigEntry(Function<Object, T> mappingFunction) {
        this.mappingFunction = mappingFunction;
    }


    @Override
    public T get() {
        return value;
    }

    void setValue(Object value) {
        this.value = mappingFunction.apply(value);
    }
}
