package com.froobworld.nabconfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigEntryMap<K, T> {
    private final Function<K, String> keyMappingFunction;
    private final Map<String, ConfigEntry<T>> entryMap = new HashMap<>();
    private final Supplier<ConfigEntry<T>> emptyConfigEntrySupplier;
    private ConfigEntry<T> def;
    private final boolean ignoreCase;

    public ConfigEntryMap(Function<K, String> keyMappingFunction, Supplier<ConfigEntry<T>> emptyConfigEntrySupplier, boolean ignoreCase) {
        this.keyMappingFunction = keyMappingFunction;
        this.emptyConfigEntrySupplier = emptyConfigEntrySupplier;
        this.def = emptyConfigEntrySupplier.get();
        this.ignoreCase = ignoreCase;
    }

    void clear() {
        entryMap.clear();
        def = emptyConfigEntrySupplier.get();
    }

    public ConfigEntry<T> of(K key) {
        String mappedKey = keyMappingFunction.apply(key);
        mappedKey = ignoreCase ? mappedKey.toLowerCase() : mappedKey;
        return entryMap.getOrDefault(mappedKey, def);
    }

    void setDefault(Object def) {
        this.def.setValue(def);
    }

    void put(String key, Object value) {
        key = ignoreCase ? key.toLowerCase() : key;
        ConfigEntry<T> configEntry = emptyConfigEntrySupplier.get();
        configEntry.setValue(value);
        entryMap.put(key, configEntry);
    }

}
