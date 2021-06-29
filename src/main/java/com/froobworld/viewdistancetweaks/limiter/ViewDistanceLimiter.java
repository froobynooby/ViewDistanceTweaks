package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ViewDistanceLimiter implements Runnable {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private final ViewDistanceHook viewDistanceHook;
    private final AdjustmentMode adjustmentMode;
    private final ManualViewDistanceManager manualViewDistanceManager;
    private final Set<ViewDistanceChangeTask> changeViewDistanceTasks = new HashSet<>();
    private final boolean logChanges;
    private final String logFormat;
    private Integer taskId;

    public ViewDistanceLimiter(ViewDistanceTweaks viewDistanceTweaks, ViewDistanceHook viewDistanceHook, AdjustmentMode adjustmentMode, ManualViewDistanceManager manualViewDistanceManager, boolean logChanges, String logFormat) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        this.viewDistanceHook = viewDistanceHook;
        this.adjustmentMode = adjustmentMode;
        this.manualViewDistanceManager = manualViewDistanceManager;
        this.logChanges = logChanges;
        this.logFormat = logFormat;
    }


    @Override
    public void run() {
        if (changeViewDistanceTasks.isEmpty()) {
            List<World> nonEmptyWorlds = Bukkit.getWorlds().stream()
                    .filter(world -> !world.getPlayers().isEmpty())
                    .filter(world -> !manualViewDistanceManager.hasManuallySetViewDistance(world))
                    .collect(Collectors.toList());
            Map<World, AdjustmentMode.Adjustment> adjustments = nonEmptyWorlds.isEmpty() ? Collections.emptyMap() : adjustmentMode.getAdjustments(nonEmptyWorlds);

            for (Map.Entry<World, AdjustmentMode.Adjustment> entry : adjustments.entrySet()) {
                World world = entry.getKey();
                AdjustmentMode.Adjustment adjustment = entry.getValue();
                int oldViewDistance = viewDistanceHook.getViewDistance(world);

                if (adjustment != AdjustmentMode.Adjustment.STAY) {
                    int newViewDistance = oldViewDistance + (adjustment == AdjustmentMode.Adjustment.INCREASE ? 1 : -1);
                    ViewDistanceChangeTask changeTask = new ViewDistanceChangeTask(viewDistanceTweaks, viewDistanceHook, world, newViewDistance, 0);
                    changeTask.run();
                    changeViewDistanceTasks.add(changeTask);
                    if (logChanges) {
                        viewDistanceTweaks.getLogger().info(MessageFormat.format(logFormat, world.getName(), oldViewDistance, newViewDistance));
                    }
                }
            }
        }
        changeViewDistanceTasks.removeIf(ViewDistanceChangeTask::completed);
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
            changeViewDistanceTasks.forEach(ViewDistanceChangeTask::cancel);
            changeViewDistanceTasks.clear();
            taskId = null;
        }
    }

}
