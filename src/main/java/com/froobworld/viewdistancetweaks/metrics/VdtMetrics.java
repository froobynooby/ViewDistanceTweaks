package com.froobworld.viewdistancetweaks.metrics;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.tick.PaperTickHook;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;

public final class VdtMetrics {

    private VdtMetrics() {}

    public static void addCustomMetrics(Metrics metrics, ViewDistanceTweaks viewDistanceTweaks) {
        metrics.addCustomChart(new Metrics.SimplePie("adjustment_mode", () -> viewDistanceTweaks.getVdtConfig().adjustmentMode.get().name()));
        metrics.addCustomChart(new Metrics.SimplePie("reactive_mode_indicator", () -> {
            if (viewDistanceTweaks.getVdtConfig().adjustmentMode.get() != AdjustmentMode.Mode.PROACTIVE) {
                if (PaperTickHook.isCompatible() && viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.useAlternativeSettings.get()) {
                    return "MSPT";
                }
                return "TPS";
            }
            return null;
        }));
    }

}
