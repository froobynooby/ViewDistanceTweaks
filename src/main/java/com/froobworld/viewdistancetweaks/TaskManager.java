package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.hook.tick.PaperTickHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.limiter.ManualViewDistanceManager;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceClamper;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceLimiter;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.*;
import com.froobworld.viewdistancetweaks.util.MsptTracker;
import com.froobworld.viewdistancetweaks.util.TpsTracker;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

public class TaskManager {
    private ViewDistanceTweaks viewDistanceTweaks;
    private TpsTracker tpsTracker;
    private MsptTracker msptTracker;
    private ViewDistanceLimiter limiterTask;
    private ViewDistanceLimiter noTickLimiterTask;
    private ViewDistanceClamper simulationDistanceClamper;
    private ViewDistanceClamper ViewDistanceClamper;
    private ManualViewDistanceManager manualSimulationDistanceManager;
    private ManualViewDistanceManager manualViewDistanceManager;

    public TaskManager(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }


    public void init() {
        initTpsTracker();
        initMsptTracker();
        initViewDistanceClampers();
        initManualViewDistanceManagers();
        initLimiterTask();
        initNoTickLimiterTask();
    }

    public void reload() {
        tpsTracker.unregister();
        msptTracker.unregister();
        limiterTask.cancel();
        if (noTickLimiterTask != null) {
            noTickLimiterTask.cancel();
        }
        manualSimulationDistanceManager.cancel();
        if (manualViewDistanceManager != null) {
            manualViewDistanceManager.cancel();
        }
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

    private void initMsptTracker() {
        msptTracker = new MsptTracker(
                viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.msptTracker.collectionPeriod.get(),
                viewDistanceTweaks.getHookManager().getTickHook()
        );
        msptTracker.register();
    }

    private void initManualViewDistanceManagers() {
        manualSimulationDistanceManager = new ManualViewDistanceManager(
                viewDistanceTweaks,
                viewDistanceTweaks.getHookManager().getSimulationDistanceHook()
        );
        SimulationDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        if (noTickViewDistanceHook != null) {
            manualViewDistanceManager = new ManualViewDistanceManager(
                    viewDistanceTweaks,
                    noTickViewDistanceHook
            );
        }
    }

    private void initLimiterTask() {
        AdjustmentMode proactiveAdjustmentMode = null;
        AdjustmentMode reactiveAdjustmentMode = null;
        AdjustmentMode.Mode mode = viewDistanceTweaks.getVdtConfig().adjustmentMode.get();
        if (mode == AdjustmentMode.Mode.REACTIVE || mode == AdjustmentMode.Mode.MIXED) {
            if (PaperTickHook.isCompatible() && viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.useAlternativeSettings.get()) {
                reactiveAdjustmentMode = new AlternativeReactiveAdjustmentMode(
                        msptTracker,
                        viewDistanceTweaks.getHookManager().getChunkCounter(),
                        viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.increaseMsptThreshold.get(),
                        viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.decreaseMsptThreshold.get(),
                        viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.msptPrediction.historyLength.get(),
                        viewDistanceTweaks.getVdtConfig().paperSettings.alternativeReactiveModeSettings.msptPrediction.enabled.get(),
                        viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.exclude.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
                );
            } else {
                reactiveAdjustmentMode = new ReactiveAdjustmentMode(
                        tpsTracker,
                        viewDistanceTweaks.getHookManager().getChunkCounter(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.increaseTpsThreshold.get(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.decreaseTpsThreshold.get(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.tpsPrediction.historyLength.get(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.tpsPrediction.enabled.get(),
                        viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.exclude.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
                );
            }
        }
        if (mode == AdjustmentMode.Mode.PROACTIVE || mode == AdjustmentMode.Mode.MIXED) {
            proactiveAdjustmentMode = new ProactiveAdjustmentMode(
                    viewDistanceTweaks.getVdtConfig().proactiveMode.globalTickingChunkCountTarget.get(),
                    viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                    viewDistanceTweaks.getHookManager().getChunkCounter(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.exclude.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
        }
        AdjustmentMode adjustmentMode = mode == AdjustmentMode.Mode.REACTIVE ? reactiveAdjustmentMode
                : (mode == AdjustmentMode.Mode.PROACTIVE ? proactiveAdjustmentMode
                : new MixedAdjustmentMode(proactiveAdjustmentMode, reactiveAdjustmentMode, AdjustmentMode.Adjustment::strongest));

        limiterTask = new ViewDistanceLimiter(
                viewDistanceTweaks,
                viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                adjustmentMode,
                manualSimulationDistanceManager,
                viewDistanceTweaks.getVdtConfig().logChanges.get(),
                "Changed simulation distance of {0} ({1} -> {2})"
        );
        limiterTask.start(viewDistanceTweaks.getVdtConfig().ticksPerCheck.get(), viewDistanceTweaks.getVdtConfig().startUpDelay.get());
    }

    private void initNoTickLimiterTask() {
        SimulationDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        if (noTickViewDistanceHook != null) {
            AdjustmentMode noTickAdjustmentMode = new ProactiveAdjustmentMode(
                    viewDistanceTweaks.getVdtConfig().proactiveMode.globalNonTickingChunkCountTarget.get(),
                    noTickViewDistanceHook,
                    viewDistanceTweaks.getHookManager().getNoTickChunkCounter(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.exclude.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.minimumViewDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
            noTickLimiterTask = new ViewDistanceLimiter(
                    viewDistanceTweaks,
                    noTickViewDistanceHook,
                    noTickAdjustmentMode,
                    manualViewDistanceManager,
                    viewDistanceTweaks.getVdtConfig().logChanges.get(),
                    "Changed view distance of {0} ({1} -> {2})"
            );
            noTickLimiterTask.start(viewDistanceTweaks.getVdtConfig().ticksPerCheck.get(), viewDistanceTweaks.getVdtConfig().startUpDelay.get());
        }
    }

    private void initViewDistanceClampers() {
        simulationDistanceClamper = new ViewDistanceClamper(
                viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get()
        );

        List<World> worldsToClamp = Bukkit.getWorlds().stream()
                .filter(world -> !viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.exclude.get())
                .collect(Collectors.toList());
        simulationDistanceClamper.clampWorlds(worldsToClamp);

        worldsToClamp = Bukkit.getWorlds().stream()
                .filter(world -> !viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.exclude.get())
                .collect(Collectors.toList());
        ViewDistanceHook viewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        if (viewDistanceHook != null) {
            ViewDistanceClamper = new ViewDistanceClamper(
                    viewDistanceHook,
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.minimumViewDistance.get()
            );
            ViewDistanceClamper.clampWorlds(worldsToClamp);
        }
    }

    public TpsTracker getTpsTracker() {
        return tpsTracker;
    }

    public MsptTracker getMsptTracker() {
        return msptTracker;
    }

    public ManualViewDistanceManager getManualSimulationDistanceManager() {
        return manualSimulationDistanceManager;
    }

    public ManualViewDistanceManager getManualViewDistanceManager() {
        return manualViewDistanceManager;
    }

}
