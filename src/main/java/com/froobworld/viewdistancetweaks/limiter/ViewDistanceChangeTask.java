package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ViewDistanceChangeTask implements Runnable {
    private ViewDistanceTweaks viewDistanceTweaks;
    private ViewDistanceHook viewDistanceHook;
    private Integer currentTaskId;
    private World world;
    private int newViewDistance;
    private long changePeriod;
    private boolean completed = false;

    public ViewDistanceChangeTask(ViewDistanceTweaks viewDistanceTweaks, ViewDistanceHook viewDistanceHook, World world, int newViewDistance, long changePeriod) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        this.viewDistanceHook = viewDistanceHook;
        this.world = world;
        this.newViewDistance = newViewDistance;
        this.changePeriod = changePeriod;
    }


    @Override
    public void run() {
        if (changePeriod <= 0) {
            viewDistanceHook.setViewDistance(world, newViewDistance);
            completed = true;
        } else {
            int currentViewDistance = viewDistanceHook.getViewDistance(world);
            int diff = Integer.compare(newViewDistance, currentViewDistance);
            viewDistanceHook.setViewDistance(world, currentViewDistance + diff);
            if (currentViewDistance + diff != newViewDistance) {
                currentTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(viewDistanceTweaks, this, changePeriod);
            } else {
                completed = true;
            }
        }
    }

    public void cancel() {
        if (currentTaskId != null) {
            Bukkit.getScheduler().cancelTask(currentTaskId);
        }
    }

    public boolean completed() {
        return completed;
    }

}
