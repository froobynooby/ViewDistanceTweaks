package com.froobworld.viewdistancetweaks.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class PreferenceChooser<T> {
    private final Map<Supplier<?>, BooleanSupplier> choiceConditionMap = new LinkedHashMap<>();

    private PreferenceChooser() {}


    public static <T> PreferenceChooser<T> bestChoice(T choice, BooleanSupplier condition) {
        return bestChoice(() -> choice, condition);
    }

    public static <T> PreferenceChooser<T> bestChoice(Supplier<T> choice, BooleanSupplier condition) {
        return new PreferenceChooser().nextBestChoice(choice, condition);
    }

    public PreferenceChooser nextBestChoice(Supplier<?> choice, BooleanSupplier condition) {
        choiceConditionMap.put(choice, condition);
        return this;
    }

    public PreferenceChooser nextBestChoice(Object choice, BooleanSupplier condition) {
        return nextBestChoice(() -> choice, condition);
    }

    public T defaultChoice(Object choice) {
        return defaultChoice(() -> choice);
    }

    public T defaultChoice(Supplier<?> choice) {
        for (Map.Entry<Supplier<?>, BooleanSupplier> entry : choiceConditionMap.entrySet()) {
            if (entry.getValue().getAsBoolean()) {
                return (T) entry.getKey().get();
            }
        }
        return (T) choice.get();
    }

    public T get() {
        return defaultChoice((Object) null);
    }

}
