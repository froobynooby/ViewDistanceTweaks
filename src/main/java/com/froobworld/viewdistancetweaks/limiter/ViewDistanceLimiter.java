package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ViewDistanceLimiter implements Runnable {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private final SimulationDistanceHook simulationDistanceHook;
    private final AdjustmentMode adjustmentMode;
    private final ManualViewDistanceManager manualViewDistanceManager;
    private final boolean logChanges;
    private final String logFormat;
    private Integer taskId;

    public ViewDistanceLimiter(ViewDistanceTweaks viewDistanceTweaks, SimulationDistanceHook simulationDistanceHook, AdjustmentMode adjustmentMode, ManualViewDistanceManager manualViewDistanceManager, boolean logChanges, String logFormat) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        this.simulationDistanceHook = simulationDistanceHook;
        this.adjustmentMode = adjustmentMode;
        this.manualViewDistanceManager = manualViewDistanceManager;
        this.logChanges = logChanges;
        this.logFormat = logFormat;
    }


    @Override
    public void run() {
        List<World> nonEmptyWorlds = Bukkit.getWorlds().stream()
                .filter(world -> !world.getPlayers().isEmpty())
                .filter(world -> !manualViewDistanceManager.hasManuallySetViewDistance(world))
                .collect(Collectors.toList());
        Map<World, AdjustmentMode.Adjustment> adjustments = nonEmptyWorlds.isEmpty() ? Collections.emptyMap() : adjustmentMode.getAdjustments(nonEmptyWorlds);

        for (Map.Entry<World, AdjustmentMode.Adjustment> entry : adjustments.entrySet()) {
            World world = entry.getKey();
            AdjustmentMode.Adjustment adjustment = entry.getValue();
            int oldViewDistance = simulationDistanceHook.getDistance(world);

            if (adjustment != AdjustmentMode.Adjustment.STAY) {
                int newViewDistance = oldViewDistance + (adjustment == AdjustmentMode.Adjustment.INCREASE ? 1 : -1);
                simulationDistanceHook.setDistance(world, newViewDistance);
                if (logChanges) {
                    viewDistanceTweaks.getLogger().info(MessageFormat.format(logFormat, world.getName(), oldViewDistance, newViewDistance));
                }
            }
        }
    }

    public void start(long period, long startUpDelay) {
        if (taskId != null) {
            throw new IllegalStateException("Already started.");
        }
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(viewDistanceTweaks, this, startUpDelay + period, period);
    }

    public void cancel() {
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = null;
        }
    }

}
