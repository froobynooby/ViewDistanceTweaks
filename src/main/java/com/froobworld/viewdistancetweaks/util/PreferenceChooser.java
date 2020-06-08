package com.froobworld.viewdistancetweaks.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class PreferenceChooser {
    private final Map<Supplier<?>, BooleanSupplier> choiceConditionMap = new LinkedHashMap<>();

    private PreferenceChooser() {}


    public static PreferenceChooser bestChoice(Object choice, BooleanSupplier condition) {
        return bestChoice(() -> choice, condition);
    }

    public static PreferenceChooser bestChoice(Supplier<?> choice, BooleanSupplier condition) {
        return new PreferenceChooser().nextBestChoice(choice, condition);
    }

    public PreferenceChooser nextBestChoice(Supplier<?> choice, BooleanSupplier condition) {
        choiceConditionMap.put(choice, condition);
        return this;
    }

    public PreferenceChooser nextBestChoice(Object choice, BooleanSupplier condition) {
        return nextBestChoice(() -> choice, condition);
    }

    public <T> T defaultChoice(T choice) {
        return defaultChoice(() -> choice);
    }

    public <T> T defaultChoice(Supplier<T> choice) {
        for (Map.Entry<Supplier<?>, BooleanSupplier> entry : choiceConditionMap.entrySet()) {
            if (entry.getValue().getAsBoolean()) {
                return (T) entry.getKey().get();
            }
        }
        return choice.get();
    }

    public <T> T get() {
        return defaultChoice((T) null);
    }

}
