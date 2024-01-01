package com.froobworld.nabconfiguration;

import java.util.ArrayList;
import java.util.List;

public final class ConfigEntries {


    public static ConfigEntry<Double> doubleEntry() {
        return new ConfigEntry<>(o -> ((Number) o).doubleValue());
    }

    public static ConfigEntry<Float> floatEntry() {
        return new ConfigEntry<>(o -> ((Number) o).floatValue());
    }

    public static ConfigEntry<Long> longEntry() {
        return new ConfigEntry<>(o -> ((Number) o).longValue());
    }

    public static ConfigEntry<Integer> integerEntry() {
        return new ConfigEntry<>(o -> ((Number) o).intValue());
    }

    public static ConfigEntry<Short> shortEntry() {
        return new ConfigEntry<>(o -> ((Number) o).shortValue());
    }

    public static ConfigEntry<Byte> byteEntry() {
        return new ConfigEntry<>(o -> ((Number) o).byteValue());
    }

    public static <E extends Enum<E>> ConfigEntry<E> enumEntry(Class<E> e) {
        return enumEntry(e, false);
    }

    public static <E extends Enum<E>> ConfigEntry<E> enumEntry(Class<E> e, boolean caseSensitive) {
        return new ConfigEntry<>(o -> {
            for (E value : e.getEnumConstants()) {
                if (caseSensitive ? value.name().equals(o.toString()) : value.name().equalsIgnoreCase(o.toString())) {
                    return value;
                }
            }
            return null;
        });
    }
    
    public static ConfigEntry<List<String>> stringListEntry() {
        return new ConfigEntry<>(o -> o == null ? new ArrayList<>() : (List<String>) o);
    }

}
