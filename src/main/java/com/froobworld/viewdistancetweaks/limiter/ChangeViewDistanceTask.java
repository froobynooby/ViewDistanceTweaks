package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class ChangeViewDistanceTask implements Runnable {
    private Plugin plugin;
    private World world;
    private int targetViewDistance;
    private long period;
    private boolean completed;

    public ChangeViewDistanceTask(Plugin plugin, World world, int targetViewDistance, long period) {
        this.plugin = plugin;
        this.world = world;
        this.targetViewDistance = ViewDistanceUtils.clampViewDistance(targetViewDistance);
        this.period = period;
    }

    @Override
    public void run() {
        if (period <= 0) {
            ViewDistanceUtils.setViewDistance(world, targetViewDistance);
            completed = true;
        } else {
            int diff = Integer.compare(targetViewDistance, world.getViewDistance());
            ViewDistanceUtils.setViewDistance(world, world.getViewDistance() + diff);
            if (world.getViewDistance() == targetViewDistance) {
                completed = true;
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, period);
            }
        }
    }

    public boolean completed() {
        return completed;
    }

}
