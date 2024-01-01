package com.froobworld.nabconfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConfigSectionMap<K, C extends ConfigSection> {
    private final Function<K, String> keyMappingFunction;
    private final Map<String, C> sectionMap = new HashMap<>();
    private final Class<C> entryType;
    private final boolean ignoreCase;
    private C defaultSection;

    public ConfigSectionMap(Function<K, String> keyMappingFunction, Class<C> entryType, boolean ignoreCase) {
        this.keyMappingFunction = keyMappingFunction;
        this.entryType = entryType;
        this.ignoreCase = ignoreCase;
    }

    void clear() {
        sectionMap.clear();
        defaultSection = null;
    }

    void setDefaultSection(C defaultSection) {
        this.defaultSection = defaultSection;
    }

    void put(String key, C configSection) {
        key = ignoreCase ? key.toLowerCase() : key;
        sectionMap.put(key, configSection);
    }

    public C of(K key) {
        String mappedKey = keyMappingFunction.apply(key);
        mappedKey = ignoreCase ? mappedKey.toLowerCase() : mappedKey;
        return sectionMap.getOrDefault(mappedKey, defaultSection);
    }

    Class<C> entryType() {
        return entryType;
    }

}
