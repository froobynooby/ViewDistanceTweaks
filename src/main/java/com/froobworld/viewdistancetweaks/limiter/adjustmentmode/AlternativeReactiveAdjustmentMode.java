package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.util.MsptTracker;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AlternativeReactiveAdjustmentMode extends BaseAdjustmentMode {
    private final MsptChunkHistory msptChunkHistory;
    private final ViewDistanceHook viewDistanceHook;
    private final MsptTracker msptTracker;
    private final ChunkCounter chunkCounter;
    private final double increaseMsptThreshold;
    private final double decreaseMsptThreshold;
    private final boolean useMsptChunkHistory;

    public AlternativeReactiveAdjustmentMode(MsptTracker msptTracker, ChunkCounter chunkCounter, double increaseMsptThreshold, double decreaseMsptThreshold, long msptChunkHistoryLength,
                                             boolean useMsptChunkHistory, ViewDistanceHook viewDistanceHook, Function<World, Integer> maxViewDistance,
                                             Function<World, Integer> minViewDistance, int requiredIncrease, int requiredDecrease) {
        super(viewDistanceHook, maxViewDistance, minViewDistance, requiredIncrease, requiredDecrease);
        this.msptTracker = msptTracker;
        this.viewDistanceHook = viewDistanceHook;
        this.chunkCounter = chunkCounter;
        this.increaseMsptThreshold = increaseMsptThreshold;
        this.decreaseMsptThreshold = decreaseMsptThreshold;
        msptChunkHistory = new MsptChunkHistory(msptChunkHistoryLength);
        this.useMsptChunkHistory = useMsptChunkHistory;
    }

    @Override
    public Map<World, Adjustment> getAdjustments(Collection<World> worlds) {
        Map<World, Integer> chunkCounts = new HashMap<>();
        int totalCount = 0;
        for (World world : worlds) {
            totalCount += chunkCounts.computeIfAbsent(world, w -> (int) chunkCounter.countChunks(w, viewDistanceHook.getViewDistance(world)));
        }

        double mspt = msptTracker.getMspt();
        msptChunkHistory.addRecord(mspt, totalCount);
        Map<World, Adjustment> adjustments = new HashMap<>();
        for (World world : worlds) {
            if (mspt <= increaseMsptThreshold) {
                if (useMsptChunkHistory && msptChunkHistory.getHighestMspt(totalCount) >= decreaseMsptThreshold) {
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
            } else if (mspt >= decreaseMsptThreshold) {
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
        msptChunkHistory.purge();
        return adjustments;
    }

    private static class MsptChunkHistory {
        private final long historyLengthMillis;
        private final Map<Long, MsptChunkRecord> records = new HashMap<>();

        public MsptChunkHistory(long historyLength) {
            historyLengthMillis = TimeUnit.MINUTES.toMillis(historyLength);
        }


        public double getHighestMspt(int chunkCount) {
            double curMax = 0;
            for (MsptChunkRecord record : records.values()) {
                if (record.chunkCount >= chunkCount) {
                    curMax = Math.max(curMax, record.mspt);
                }
            }
            return curMax;
        }

        public void addRecord(double mspt, int chunkCount) {
            records.put(System.currentTimeMillis(), new MsptChunkRecord(mspt, chunkCount));
        }

        public void purge() {
            long curTimeMillis = System.currentTimeMillis();
            records.entrySet().removeIf(entry -> curTimeMillis - entry.getKey() > historyLengthMillis);
        }

        private static class MsptChunkRecord {
            public final double mspt;
            public final int chunkCount;

            public MsptChunkRecord(double mspt, int chunkCount) {
                this.mspt = mspt;
                this.chunkCount = chunkCount;
            }

        }

    }

}
