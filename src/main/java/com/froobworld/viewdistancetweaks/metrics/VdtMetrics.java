package com.froobworld.viewdistancetweaks.metrics;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;

public final class VdtMetrics {

    private VdtMetrics() {};

    public static void addCustomMetrics(Metrics metrics, ViewDistanceTweaks viewDistanceTweaks) {
        metrics.addCustomChart(new Metrics.SimplePie("adjustment_mode", () -> viewDistanceTweaks.getVdtConfig().adjustmentMode.get().name()));
    }

}
