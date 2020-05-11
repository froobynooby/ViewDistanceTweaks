package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.util.TpsTracker;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ReactiveAdjustmentMode extends BaseAdjustmentMode {
    private final TpsChunkHistory tpsChunkHistory;
    private final ViewDistanceHook viewDistanceHook;
    private final TpsTracker tpsTracker;
    private final ChunkCounter chunkCounter;
    private final double increaseTpsThreshold;
    private final double decreaseTpsThreshold;
    private final boolean useTpsChunkHistory;

    public ReactiveAdjustmentMode(TpsTracker tpsTracker, ChunkCounter chunkCounter, double increaseTpsThreshold, double decreaseTpsThreshold, long tpsChunkHistoryLength,
                                  boolean useTpsChunkHistory, ViewDistanceHook viewDistanceHook, Function<World, Integer> maxViewDistance,
                                  Function<World, Integer> minViewDistance, int requiredIncrease, int requiredDecrease) {
        super(viewDistanceHook, maxViewDistance, minViewDistance, requiredIncrease, requiredDecrease);
        this.tpsTracker = tpsTracker;
        this.viewDistanceHook = viewDistanceHook;
        this.chunkCounter = chunkCounter;
        this.increaseTpsThreshold = increaseTpsThreshold;
        this.decreaseTpsThreshold = decreaseTpsThreshold;
        tpsChunkHistory = new TpsChunkHistory(tpsChunkHistoryLength);
        this.useTpsChunkHistory = useTpsChunkHistory;
    }

    @Override
    public Map<World, Adjustment> getAdjustments(Collection<World> worlds) {
        Map<World, Integer> chunkCounts = new HashMap<>();
        int totalCount = 0;
        for (World world : worlds) {
            totalCount += chunkCounts.computeIfAbsent(world, w -> (int) chunkCounter.countChunks(w, viewDistanceHook.getViewDistance(world)));
        }

        double tps = tpsTracker.getTps();
        tpsChunkHistory.addRecord(tps, totalCount);
        Map<World, Adjustment> adjustments = new HashMap<>();
        for (World world : worlds) {
            if (tps >= increaseTpsThreshold) {
                if (useTpsChunkHistory && tpsChunkHistory.getLowestTps(totalCount) <= decreaseTpsThreshold) {
                    adjustments.put(world, tryStay(world));
                } else {
                    Adjustment adjustment = tryIncrease(world);
                    adjustments.put(world, adjustment);
                    if (adjustment == Adjustment.INCREASE) {
                        int oldChunkCount = chunkCounts.get(world);
                        int newChunkCount = (int) chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world) + 1);
                        totalCount += newChunkCount - oldChunkCount;
                    }
                }
            } else if (tps <= decreaseTpsThreshold) {
                Adjustment adjustment = tryDecrease(world);
                adjustments.put(world, adjustment);
                if (adjustment == Adjustment.DECREASE) {
                    int oldChunkCount = chunkCounts.get(world);
                    int newChunkCount = (int) chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world) - 1);
                    totalCount += newChunkCount - oldChunkCount;
                }
            } else {
                adjustments.put(world, Adjustment.STAY);
            }
        }
        tpsChunkHistory.purge();
        return adjustments;
    }

    private static class TpsChunkHistory {
        private final long historyLengthMillis;
        private final Map<Long, TpsChunkRecord> records = new HashMap<>();

        public TpsChunkHistory(long historyLength) {
            historyLengthMillis = TimeUnit.MINUTES.toMillis(historyLength);
        }


        public double getLowestTps(int chunkCount) {
            final double[] curMin = {20.0};
            records.values().forEach(record -> {
                if (record.chunkCount <= chunkCount) {
                    curMin[0] = Math.min(curMin[0], record.tps);
                }
            });

            return curMin[0];
        }

        public void addRecord(double tps, int chunkCount) {
            records.put(System.currentTimeMillis(), new TpsChunkRecord(tps, chunkCount));
        }

        public void purge() {
            long curTimeMillis = System.currentTimeMillis();
            records.entrySet().removeIf(entry -> curTimeMillis - entry.getKey() > historyLengthMillis);
        }

        private static class TpsChunkRecord {
            public final double tps;
            public final int chunkCount;

            public TpsChunkRecord(double tps, int chunkCount) {
                this.tps = tps;
                this.chunkCount = chunkCount;
            }

        }

    }

}
