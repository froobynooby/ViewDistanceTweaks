package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManualViewDistanceManager {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private final Map<UUID, Integer> worldTaskMap = new HashMap<>();
    private final ViewDistanceHook viewDistanceHook;
    private final ViewDistanceClamper viewDistanceClamper;

    public ManualViewDistanceManager(ViewDistanceTweaks viewDistanceTweaks, ViewDistanceHook viewDistanceHook, ViewDistanceClamper viewDistanceClamper) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        this.viewDistanceHook = viewDistanceHook;
        this.viewDistanceClamper = viewDistanceClamper;
    }


    public void setViewDistance(World world, int viewDistance, long durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        viewDistanceHook.setViewDistance(world, viewDistance);
        Integer lastTaskId = worldTaskMap.remove(world.getUID());
        if (lastTaskId != null) {
            Bukkit.getScheduler().cancelTask(lastTaskId);
        }
        worldTaskMap.put(world.getUID(), Bukkit.getScheduler().scheduleSyncDelayedTask(viewDistanceTweaks, () -> {
            viewDistanceClamper.clampWorld(world);
            worldTaskMap.remove(world.getUID());
        }, durationTicks));
    }

    public void clearManualViewDistance(World world) {
        Integer lastTaskId = worldTaskMap.remove(world.getUID());
        if (lastTaskId != null) {
            Bukkit.getScheduler().cancelTask(lastTaskId);
            viewDistanceClamper.clampWorld(world);
        }
    }

    public boolean hasManuallySetViewDistance(World world) {
        return worldTaskMap.containsKey(world.getUID());
    }

    public void cancel() {
        for (int taskId : worldTaskMap.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        worldTaskMap.clear();
    }

}
