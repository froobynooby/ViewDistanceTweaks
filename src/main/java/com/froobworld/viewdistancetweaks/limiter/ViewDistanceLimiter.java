package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

public class ViewDistanceLimiter {
    private Map<UUID, ViewDistanceCheckResultHistory> checkHistoryMap = new HashMap<>();

    private ViewDistanceTweaks viewDistanceTweaks;
    private List<ChangeViewDistanceTask> runningChangeViewDistanceTasks = new ArrayList<>();

    public ViewDistanceLimiter(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        new CheckTask().run();
    }

    private boolean changeViewDistanceTasksRunning() {
        for (ChangeViewDistanceTask task : runningChangeViewDistanceTasks) {
            if (!task.completed()) {
                return true;
            }
        }
        runningChangeViewDistanceTasks.clear();
        return false;
    }

    public class CheckTask implements Runnable {

        @Override
        public void run() {
            if (Bukkit.getOnlinePlayers().size() > 0 && !changeViewDistanceTasksRunning()) {
                double weightedPlayerCount = 0;
                for (World world : Bukkit.getWorlds()) {
                    double chunkWeight = viewDistanceTweaks.getViewDistanceTweaksConfig().getChunkWeight(world);
                    weightedPlayerCount += world.getPlayers().size() * chunkWeight;
                }
                double chunkShare = weightedPlayerCount == 0 ? Integer.MAX_VALUE : viewDistanceTweaks.getViewDistanceTweaksConfig().getTargetGlobalChunkCount() / weightedPlayerCount;
                for (World world : Bukkit.getWorlds()) {
                    checkHistoryMap.putIfAbsent(world.getUID(), new ViewDistanceCheckResultHistory());
                    double chunkWeight = viewDistanceTweaks.getViewDistanceTweaksConfig().getChunkWeight(world);
                    double weightedChunkShare = chunkWeight == 0 ? Integer.MAX_VALUE : chunkShare / chunkWeight;
                    int newViewDistance = (int) Math.min(viewDistanceTweaks.getViewDistanceTweaksConfig().getMaximumViewDistance(world),
                            Math.max(ViewDistanceUtils.viewDistanceFromChunkCount(weightedChunkShare), viewDistanceTweaks.getViewDistanceTweaksConfig().getMinimumViewDistance(world)));
                    boolean doChange = false;
                    if (newViewDistance > ViewDistanceUtils.getViewDistance(world)) {
                        if (checkHistoryMap.get(world.getUID()).increase()
                                >= viewDistanceTweaks.getViewDistanceTweaksConfig().getPassedChecksForIncrease()) {
                            doChange = true;
                        }
                    } else if (newViewDistance < ViewDistanceUtils.getViewDistance(world)) {
                        if (checkHistoryMap.get(world.getUID()).decrease()
                                >= viewDistanceTweaks.getViewDistanceTweaksConfig().getPassedChecksForDecrease()) {
                            doChange = true;
                        }
                    }

                    if (doChange) {
                        checkHistoryMap.put(world.getUID(), new ViewDistanceCheckResultHistory());
                        ChangeViewDistanceTask changeTask = new ChangeViewDistanceTask(viewDistanceTweaks, world,
                                newViewDistance, viewDistanceTweaks.getViewDistanceTweaksConfig().getSmoothChangePeriod());
                        changeTask.run();
                        runningChangeViewDistanceTasks.add(changeTask);
                    }
                }


            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(viewDistanceTweaks, this,
                    Math.max(1, viewDistanceTweaks.getViewDistanceTweaksConfig().getTicksPerCheck()));
        }

    }

    private static class ViewDistanceCheckResultHistory {
        private int increaseCount;
        private int decreaseCount;

        public int increase() {
            decreaseCount = 0;
            increaseCount++;
            return increaseCount;
        }

        public int decrease() {
            increaseCount = 0;
            decreaseCount++;
            return decreaseCount;
        }

    }

}
