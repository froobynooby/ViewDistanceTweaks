package com.froobworld.viewdistancetweaks;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.List;
import java.util.stream.Collectors;

public class TaskManager {
    private ViewDistanceTweaks viewDistanceTweaks;
    private TpsTracker tpsTracker;
    private MsptTracker msptTracker;
    private ViewDistanceLimiter limiterTask;
    private ViewDistanceLimiter noTickLimiterTask;
    private ViewDistanceClamper simulationDistanceClamper;
    private ViewDistanceClamper viewDistanceClamper;
    private AdjustmentModes adjustmentModes;
    private Listener clamperListener;
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
        adjustmentModes = new AdjustmentModes(viewDistanceTweaks);
        adjustmentModes.init();
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
                viewDistanceTweaks.getHookManager().getTickHook()
        );
        tpsTracker.register();
    }

    private void initMsptTracker() {
        msptTracker = new MsptTracker(
                viewDistanceTweaks.getVdtConfig().reactiveMode.msptTracker.collectionPeriod.get(),
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
        limiterTask = new ViewDistanceLimiter(
                viewDistanceTweaks,
                viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                adjustmentModes.getSimulationDistanceAdjustmentMode(),
                manualSimulationDistanceManager,
                viewDistanceTweaks.getVdtConfig().logChanges.get(),
                "Changed simulation distance of {0} ({1} -> {2})"
        );
        limiterTask.start(viewDistanceTweaks.getVdtConfig().ticksPerCheck.get(), viewDistanceTweaks.getVdtConfig().startUpDelay.get());
    }

    private void initNoTickLimiterTask() {
        SimulationDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        if (noTickViewDistanceHook != null) {
            noTickLimiterTask = new ViewDistanceLimiter(
                    viewDistanceTweaks,
                    noTickViewDistanceHook,
                    adjustmentModes.getViewDistanceAdjustmentMode(),
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
            viewDistanceClamper = new ViewDistanceClamper(
                    viewDistanceHook,
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.minimumViewDistance.get()
            );
            viewDistanceClamper.clampWorlds(worldsToClamp);
        }
        if (clamperListener == null) {
            clamperListener = new Listener() {
                @EventHandler
                void onWorldLoad(WorldLoadEvent event) {
                    if (!viewDistanceTweaks.getVdtConfig().worldSettings.of(event.getWorld()).simulationDistance.exclude.get()) {
                        simulationDistanceClamper.clampWorld(event.getWorld());
                    }
                    if (viewDistanceClamper != null && !viewDistanceTweaks.getVdtConfig().worldSettings.of(event.getWorld()).viewDistance.exclude.get()) {
                        viewDistanceClamper.clampWorld(event.getWorld());
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(clamperListener, viewDistanceTweaks);
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
