package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManualViewDistanceManager {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private final Map<UUID, Integer> worldTaskMap = new HashMap<>();
    private final Map<UUID, Integer> previousViewDistance = new HashMap<>();
    private final SimulationDistanceHook simulationDistanceHook;

    public ManualViewDistanceManager(ViewDistanceTweaks viewDistanceTweaks, SimulationDistanceHook simulationDistanceHook) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        this.simulationDistanceHook = simulationDistanceHook;
    }


    public void setViewDistance(World world, int viewDistance, long durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        if (!previousViewDistance.containsKey(world.getUID())) {
            previousViewDistance.put(world.getUID(), simulationDistanceHook.getDistance(world));
        }
        simulationDistanceHook.setDistance(world, viewDistance);
        Integer lastTaskId = worldTaskMap.remove(world.getUID());
        if (lastTaskId != null) {
            Bukkit.getScheduler().cancelTask(lastTaskId);
        }
        worldTaskMap.put(world.getUID(), Bukkit.getScheduler().scheduleSyncDelayedTask(viewDistanceTweaks, () -> {
            simulationDistanceHook.setDistance(world, previousViewDistance.remove(world.getUID()));
            worldTaskMap.remove(world.getUID());
        }, durationTicks));
    }

    public boolean hasManuallySetViewDistance(World world) {
        return worldTaskMap.containsKey(world.getUID());
    }

    public void cancel() {
        for (int taskId : worldTaskMap.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        for (World world : Bukkit.getWorlds()) {
            if (previousViewDistance.containsKey(world.getUID())) {
                simulationDistanceHook.setDistance(world, previousViewDistance.get(world.getUID()));
            }
        }
        worldTaskMap.clear();
    }

}
