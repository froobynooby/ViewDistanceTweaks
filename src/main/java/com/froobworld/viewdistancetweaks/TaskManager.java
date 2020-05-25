package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.limiter.ManualViewDistanceManager;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceClamper;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceLimiter;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.MixedAdjustmentMode;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.ProactiveAdjustmentMode;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.ReactiveAdjustmentMode;
import com.froobworld.viewdistancetweaks.util.TpsTracker;
import org.bukkit.Bukkit;

public class TaskManager {
    private ViewDistanceTweaks viewDistanceTweaks;
    private TpsTracker tpsTracker;
    private ViewDistanceLimiter limiterTask;
    private ViewDistanceLimiter noTickLimiterTask;
    private ViewDistanceClamper viewDistanceClamper;
    private ViewDistanceClamper noTickViewDistanceClamper;
    private ManualViewDistanceManager manualViewDistanceManager;
    private ManualViewDistanceManager manualNoTickViewDistanceManager;

    public TaskManager(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }


    public void init() {
        initTpsTracker();
        initViewDistanceClampers();
        initManualViewDistanceManagers();
        initLimiterTask();
        initNoTickLimiterTask();
    }

    public void reload() {
        tpsTracker.unregister();
        limiterTask.cancel();
        if (noTickLimiterTask != null) {
            noTickLimiterTask.cancel();
        }
        manualViewDistanceManager.cancel();
        manualNoTickViewDistanceManager.cancel();
        init();
    }

    private void initTpsTracker() {
        tpsTracker = new TpsTracker(
                viewDistanceTweaks.getVdtConfig().reactiveMode.tpsTracker.collectionPeriod.get(),
                viewDistanceTweaks.getHookManager().getTickHook(),
                viewDistanceTweaks.getVdtConfig().reactiveMode.tpsTracker.trimOutliersPercent.get()
        );
        tpsTracker.register();
    }

    private void initManualViewDistanceManagers() {
        manualViewDistanceManager = new ManualViewDistanceManager(
                viewDistanceTweaks,
                viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                viewDistanceClamper
        );
        ViewDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook();
        if (noTickViewDistanceHook != null) {
            manualNoTickViewDistanceManager = new ManualViewDistanceManager(
                    viewDistanceTweaks,
                    noTickViewDistanceHook,
                    noTickViewDistanceClamper
            );
        }
    }

    private void initLimiterTask() {
        AdjustmentMode proactiveAdjustmentMode = null;
        AdjustmentMode reactiveAdjustmentMode = null;
        AdjustmentMode.Mode mode = viewDistanceTweaks.getVdtConfig().adjustmentMode.get();
        if (mode == AdjustmentMode.Mode.REACTIVE || mode == AdjustmentMode.Mode.MIXED) {
            reactiveAdjustmentMode = new ReactiveAdjustmentMode(
                    tpsTracker,
                    viewDistanceTweaks.getHookManager().getChunkCounter(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.increaseTpsThreshold.get(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.decreaseTpsThreshold.get(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.tpsPrediction.historyLength.get(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.tpsPrediction.enabled.get(),
                    viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).maximumViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).minimumViewDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
        }
        if (mode == AdjustmentMode.Mode.PROACTIVE || mode == AdjustmentMode.Mode.MIXED) {
            proactiveAdjustmentMode = new ProactiveAdjustmentMode(
                    viewDistanceTweaks.getVdtConfig().proactiveMode.globalChunkCountTarget.get(),
                    viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                    viewDistanceTweaks.getHookManager().getChunkCounter(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).maximumViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).minimumViewDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
        }
        AdjustmentMode adjustmentMode = mode == AdjustmentMode.Mode.REACTIVE ? reactiveAdjustmentMode
                : (mode == AdjustmentMode.Mode.PROACTIVE ? proactiveAdjustmentMode
                : new MixedAdjustmentMode(proactiveAdjustmentMode, reactiveAdjustmentMode, AdjustmentMode.Adjustment::strongest));

        limiterTask = new ViewDistanceLimiter(
                viewDistanceTweaks,
                viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                adjustmentMode,
                manualViewDistanceManager,
                viewDistanceTweaks.getVdtConfig().logViewDistanceChanges.get(),
                "Changed view distance of {0} ({1} -> {2})"
        );
        limiterTask.start(viewDistanceTweaks.getVdtConfig().ticksPerCheck.get());
    }

    private void initNoTickLimiterTask() {
        ViewDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook();
        if (viewDistanceTweaks.getVdtConfig().paperSettings.noTickViewDistance.enabled.get() && noTickViewDistanceHook != null) {
            AdjustmentMode noTickAdjustmentMode = new ProactiveAdjustmentMode(
                    viewDistanceTweaks.getVdtConfig().paperSettings.noTickViewDistance.globalChunkCountTarget.get(),
                    noTickViewDistanceHook,
                    viewDistanceTweaks.getHookManager().getNoTickChunkCounter(),
                    world -> viewDistanceTweaks.getVdtConfig().paperSettings.worldSettings.of(world).maximumNoTickViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().paperSettings.worldSettings.of(world).minimumNoTickViewDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
            noTickLimiterTask = new ViewDistanceLimiter(
                    viewDistanceTweaks,
                    noTickViewDistanceHook,
                    noTickAdjustmentMode,
                    manualNoTickViewDistanceManager,
                    viewDistanceTweaks.getVdtConfig().logViewDistanceChanges.get(),
                    "Changed no-tick view distance of {0} ({1} -> {2})"
            );
            noTickLimiterTask.start(viewDistanceTweaks.getVdtConfig().ticksPerCheck.get());
        }
    }

    private void initViewDistanceClampers() {
        viewDistanceClamper = new ViewDistanceClamper(
                viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).maximumViewDistance.get(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).minimumViewDistance.get()
        );
        viewDistanceClamper.clampWorlds(Bukkit.getWorlds());

        ViewDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook();
        if (noTickViewDistanceHook != null && viewDistanceTweaks.getVdtConfig().paperSettings.noTickViewDistance.enabled.get()) {
            noTickViewDistanceClamper = new ViewDistanceClamper(
                    noTickViewDistanceHook,
                    world -> viewDistanceTweaks.getVdtConfig().paperSettings.worldSettings.of(world).maximumNoTickViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().paperSettings.worldSettings.of(world).minimumNoTickViewDistance.get()
            );
            noTickViewDistanceClamper.clampWorlds(Bukkit.getWorlds());
        }
    }

    public ManualViewDistanceManager getManualViewDistanceManager() {
        return manualViewDistanceManager;
    }

    public ManualViewDistanceManager getManualNoTickViewDistanceManager() {
        return manualNoTickViewDistanceManager;
    }

}
