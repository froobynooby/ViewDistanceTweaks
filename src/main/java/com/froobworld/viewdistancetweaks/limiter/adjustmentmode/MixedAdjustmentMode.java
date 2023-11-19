package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MixedAdjustmentMode implements AdjustmentMode {
    private final AdjustmentMode adjustmentMode1;
    private final AdjustmentMode adjustmentMode2;
    private final BiFunction<Adjustment, Adjustment, Adjustment> mergeFunction;

    public MixedAdjustmentMode(AdjustmentMode adjustmentMode1, AdjustmentMode adjustmentMode2, BiFunction<Adjustment, Adjustment, Adjustment> mergeFunction) {
        this.adjustmentMode1 = adjustmentMode1;
        this.adjustmentMode2 = adjustmentMode2;
        this.mergeFunction = mergeFunction;
    }

    @Override
    public Map<World, Adjustment> getAdjustments(Collection<World> worlds, boolean mutate) {
        Map<World, Adjustment> adjustments1 = adjustmentMode1.getAdjustments(worlds, mutate);
        Map<World, Adjustment> adjustments2 = adjustmentMode2.getAdjustments(worlds, mutate);

        Map<World, Adjustment> finalAdjustments = new HashMap<>(adjustments1);
        for (Map.Entry<World, Adjustment> entry : adjustments2.entrySet()) {
            finalAdjustments.merge(entry.getKey(), entry.getValue(), mergeFunction);
        }

        return finalAdjustments;
    }
}
