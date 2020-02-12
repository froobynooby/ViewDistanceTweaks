package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ChangeViewDistanceTask implements Runnable {
    private ViewDistanceTweaks viewDistanceTweaks;
    private World world;
    private int targetViewDistance;
    private long period;
    private boolean completed;

    public ChangeViewDistanceTask(ViewDistanceTweaks viewDistanceTweaks, World world, int targetViewDistance, long period) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        this.world = world;
        this.targetViewDistance = ViewDistanceUtils.clampViewDistance(targetViewDistance);
        this.period = period;
    }

    @Override
    public void run() {
        int from = ViewDistanceUtils.getViewDistance(world);
        if (period <= 0) {
            ViewDistanceUtils.setViewDistance(world, targetViewDistance);
            completed = true;
        } else {
            int diff = Integer.compare(targetViewDistance, ViewDistanceUtils.getViewDistance(world));
            ViewDistanceUtils.setViewDistance(world, ViewDistanceUtils.getViewDistance(world) + diff);
            if (ViewDistanceUtils.getViewDistance(world) == targetViewDistance) {
                completed = true;
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(viewDistanceTweaks, this, period);
            }
        }
        int to = ViewDistanceUtils.getViewDistance(world);
        if (viewDistanceTweaks.getViewDistanceTweaksConfig().logViewDistanceChangs() && from != to) {
            viewDistanceTweaks.getLogger().info("View distance of " + world.getName() + " changed to " + to
                    + " (was " + from + ").");
        }
    }

    public boolean completed() {
        return completed;
    }

}
