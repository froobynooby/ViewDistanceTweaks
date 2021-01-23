package com.froobworld.viewdistancetweaks.metrics;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.tick.PaperTickHook;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public final class VdtMetrics {
    private final Metrics metrics;

    public VdtMetrics(ViewDistanceTweaks viewDistanceTweaks) {
        metrics = new Metrics(viewDistanceTweaks, 6488);
        addCustomMetrics(viewDistanceTweaks);
    }

    private void addCustomMetrics(ViewDistanceTweaks viewDistanceTweaks) {
        metrics.addCustomChart(new SimplePie("adjustment_mode", () -> viewDistanceTweaks.getVdtConfig().adjustmentMode.get().name()));
        metrics.addCustomChart(new SimplePie("reactive_mode_indicator", () -> {
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
