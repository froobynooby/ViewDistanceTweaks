package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.command.VdtCommand;
import com.froobworld.viewdistancetweaks.config.VdtConfig;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.MixedAdjustmentMode;
import com.froobworld.viewdistancetweaks.util.*;
import com.froobworld.viewdistancetweaks.hook.tick.PaperTickHook;
import com.froobworld.viewdistancetweaks.hook.tick.SpigotTickHook;
import com.froobworld.viewdistancetweaks.hook.tick.TickHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.notick.NoTickViewDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.notick.PaperNoTickViewDistanceHook;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.ProactiveAdjustmentMode;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.ReactiveAdjustmentMode;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceLimiter;
import com.froobworld.viewdistancetweaks.hook.viewdistance.*;
import com.froobworld.viewdistancetweaks.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ViewDistanceTweaks extends JavaPlugin {
    private VdtConfig vdtConfig;
    private TickHook tickHook;
    private TpsTracker tpsTracker;
    private ViewDistanceHook viewDistanceHook;
    private NoTickViewDistanceHook noTickViewDistanceHook;
    private ViewDistanceLimiter viewDistanceLimiter;
    private ViewDistanceLimiter noTickViewDistanceLimiter;
    private ChunkCounter chunkCounter;
    private ChunkCounter noTickChunkCounter;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception ex) {
            getLogger().severe("ViewDistanceTweaks requires Spigot (or a fork such as Paper) in order to run.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        vdtConfig = new VdtConfig(this);
        try {
            vdtConfig.load();
        } catch (Exception e) {
            getLogger().severe("Exception while loading configuration:");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!vdtConfig.enabled.get()) {
            getLogger().warning("ViewDistanceTweaks must be configured before it can be enabled. Edit the " +
                    "config.yml file in the plugin's data folder, setting the 'enabled' option to true when you are " +
                    "done, then reload or restart the server.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ViewDistanceUtils.syncSpigotViewDistances();
        initHooks();
        initTasks();
        registerCommands();
        initMetrics();

        getLogger().info("Finished startup.");
    }

    @Override
    public void onDisable() {}

    private void initHooks() {
        viewDistanceHook = PreferenceChooser
                .bestChoice(PaperViewDistanceHook::new, PaperViewDistanceHook::isCompatible)
                .defaultChoice(SpigotViewDistanceHook::new);
        getLogger().info("Using " + viewDistanceHook.getClass().getSimpleName() + " for the view distance hook.");

        noTickViewDistanceHook = PreferenceChooser
                .bestChoice(PaperNoTickViewDistanceHook::new, PaperNoTickViewDistanceHook::isCompatible)
                .get();
        getLogger().info(noTickViewDistanceHook != null ?
                "Using " + noTickViewDistanceHook.getClass().getSimpleName() + " for the no-tick view distance hook." :
                "No hook available for no-tick view distance.");

        tickHook = PreferenceChooser
                .bestChoice(PaperTickHook::new, PaperTickHook::isCompatible)
                .defaultChoice(SpigotTickHook::new);
        tickHook.register(this);
        getLogger().info("Using " + tickHook.getClass().getSimpleName() + " for the tick hook.");

        chunkCounter = new StandardChunkCounter(
                world -> vdtConfig.worldSettings.of(world).chunkWeight.get(),
                world -> vdtConfig.worldSettings.of(world).chunkCounter.excludeOverlap.get()
        );
        noTickChunkCounter = new NoTickChunkCounter(
                viewDistanceHook,
                world -> vdtConfig.worldSettings.of(world).chunkWeight.get(),
                world -> vdtConfig.worldSettings.of(world).chunkCounter.excludeOverlap.get()
        );
    }

    private void initTasks() {
        tpsTracker = new TpsTracker(
                vdtConfig.reactiveMode.tpsTracker.collectionPeriod.get(),
                tickHook,
                vdtConfig.reactiveMode.tpsTracker.trimOutliersPercent.get()
        );
        tpsTracker.register();

        AdjustmentMode proactiveAdjustmentMode = null;
        AdjustmentMode reactiveAdjustmentMode = null;
        if (vdtConfig.adjustmentMode.get() == AdjustmentMode.Mode.REACTIVE || vdtConfig.adjustmentMode.get() == AdjustmentMode.Mode.MIXED) {
            reactiveAdjustmentMode = new ReactiveAdjustmentMode(
                    tpsTracker,
                    chunkCounter,
                    vdtConfig.reactiveMode.increaseTpsThreshold.get(),
                    vdtConfig.reactiveMode.decreaseTpsThreshold.get(),
                    vdtConfig.reactiveMode.tpsPrediction.historyLength.get(),
                    vdtConfig.reactiveMode.tpsPrediction.enabled.get(),
                    viewDistanceHook,
                    world -> vdtConfig.worldSettings.of(world).maximumViewDistance.get(),
                    world -> vdtConfig.worldSettings.of(world).minimumViewDistance.get(),
                    vdtConfig.passedChecksForIncrease.get(),
                    vdtConfig.passedChecksForDecrease.get()
            );
        }
        if (vdtConfig.adjustmentMode.get() == AdjustmentMode.Mode.PROACTIVE || vdtConfig.adjustmentMode.get() == AdjustmentMode.Mode.MIXED) {
            proactiveAdjustmentMode = new ProactiveAdjustmentMode(
                    vdtConfig.proactiveMode.globalChunkCountTarget.get(),
                    viewDistanceHook,
                    chunkCounter,
                    world -> vdtConfig.worldSettings.of(world).maximumViewDistance.get(),
                    world -> vdtConfig.worldSettings.of(world).minimumViewDistance.get(),
                    vdtConfig.passedChecksForIncrease.get(),
                    vdtConfig.passedChecksForDecrease.get()
            );
        }
        AdjustmentMode adjustmentMode = vdtConfig.adjustmentMode.get() == AdjustmentMode.Mode.REACTIVE ? reactiveAdjustmentMode
                : (vdtConfig.adjustmentMode.get() == AdjustmentMode.Mode.PROACTIVE ? proactiveAdjustmentMode
                : new MixedAdjustmentMode(proactiveAdjustmentMode, reactiveAdjustmentMode, AdjustmentMode.Adjustment::strongest));

        viewDistanceLimiter = new ViewDistanceLimiter(
                this,
                viewDistanceHook,
                adjustmentMode,
                vdtConfig.logViewDistanceChanges.get(),
                "Changed view distance of {0} ({1} -> {2})"
        );
        viewDistanceLimiter.start(vdtConfig.ticksPerCheck.get());

        if (vdtConfig.paperSettings.noTickViewDistance.enabled.get() && noTickViewDistanceHook != null) {
            AdjustmentMode noTickAdjustmentMode = new ProactiveAdjustmentMode(
                    vdtConfig.paperSettings.noTickViewDistance.globalChunkCountTarget.get(),
                    noTickViewDistanceHook,
                    noTickChunkCounter,
                    world -> vdtConfig.paperSettings.worldSettings.of(world).maximumNoTickViewDistance.get(),
                    world -> vdtConfig.paperSettings.worldSettings.of(world).minimumNoTickViewDistance.get(),
                    vdtConfig.passedChecksForIncrease.get(),
                    vdtConfig.passedChecksForDecrease.get()
            );
            noTickViewDistanceLimiter = new ViewDistanceLimiter(
                    this,
                    noTickViewDistanceHook,
                    noTickAdjustmentMode,
                    vdtConfig.logViewDistanceChanges.get(),
                    "Changed no-tick view distance of {0} ({1} -> {2})"
            );
            noTickViewDistanceLimiter.start(vdtConfig.ticksPerCheck.get());
        }
    }

    public void unregisterTasks() {
        tpsTracker.unregister();
        viewDistanceLimiter.cancel();
        if (noTickViewDistanceLimiter != null) {
            noTickViewDistanceLimiter.cancel();
        }
    }

    public void reload() throws Exception {
        unregisterTasks();
        vdtConfig.load();
        initTasks();
    }

    private void registerCommands() {
        getCommand("vdt").setExecutor(new VdtCommand(this));
        getCommand("vdt").setPermission(VdtCommand.PERMISSON);
        getCommand("vdt").setTabCompleter(VdtCommand.tabCompleter);
    }

    private void initMetrics() {
        new Metrics(this, 6488);
    }

    public VdtConfig getVdtConfig() {
        return vdtConfig;
    }

    public ViewDistanceHook getViewDistanceHook() {
        return viewDistanceHook;
    }

    public ViewDistanceHook getNoTickViewDistanceHook() {
        return noTickViewDistanceHook;
    }

    public ChunkCounter getChunkCounter() {
        return chunkCounter;
    }

    public ChunkCounter getNoTickChunkCounter() {
        return noTickChunkCounter;
    }

    public TpsTracker getTpsTracker() {
        return tpsTracker;
    }

}
