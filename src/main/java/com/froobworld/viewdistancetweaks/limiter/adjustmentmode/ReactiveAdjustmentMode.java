package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.util.MsptTracker;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ReactiveAdjustmentMode extends BaseAdjustmentMode {
    private final MsptChunkHistory msptChunkHistory;
    private final SimulationDistanceHook simulationDistanceHook;
    private final MsptTracker msptTracker;
    private final ChunkCounter chunkCounter;
    private final double increaseMsptThreshold;
    private final double decreaseMsptThreshold;
    private final boolean useMsptChunkHistory;

    public ReactiveAdjustmentMode(MsptTracker msptTracker, ChunkCounter chunkCounter, double increaseMsptThreshold, double decreaseMsptThreshold, long msptChunkHistoryLength,
                                  boolean useMsptChunkHistory, SimulationDistanceHook simulationDistanceHook, Function<World, Boolean> exclude, Function<World, Integer> maxViewDistance,
                                  Function<World, Integer> minViewDistance, int requiredIncrease, int requiredDecrease) {
        super(simulationDistanceHook, exclude, maxViewDistance, minViewDistance, requiredIncrease, requiredDecrease);
        this.msptTracker = msptTracker;
        this.simulationDistanceHook = simulationDistanceHook;
        this.chunkCounter = chunkCounter;
        this.increaseMsptThreshold = increaseMsptThreshold;
        this.decreaseMsptThreshold = decreaseMsptThreshold;
        msptChunkHistory = new MsptChunkHistory(msptChunkHistoryLength);
        this.useMsptChunkHistory = useMsptChunkHistory;
    }

    @Override
    public Map<World, Adjustment> getAdjustments(Collection<World> worlds, boolean mutate) {
        Map<World, Integer> chunkCounts = new HashMap<>();
        int totalCount = 0;
        for (World world : worlds) {
            totalCount += chunkCounts.computeIfAbsent(world, w -> (int) chunkCounter.countChunks(w, simulationDistanceHook.getDistance(world)));
        }

        double mspt = msptTracker.getMspt();
        msptChunkHistory.addRecord(mspt, totalCount);
        Map<World, Adjustment> adjustments = new HashMap<>();
        int totalAdditionalChunks = 0;
        for (World world : worlds) {
            if (mspt <= increaseMsptThreshold) {
                int additionalChunks = (int) chunkCounter.countChunks(world, simulationDistanceHook.getDistance(world) + 1) - chunkCounts.get(world);
                if (useMsptChunkHistory && mspt + msptChunkHistory.getMaximumMsptPerChunk() * (totalAdditionalChunks + additionalChunks) >= decreaseMsptThreshold) {
                    adjustments.put(world, tryStay(world, mutate));
                } else {
                    Adjustment adjustment = tryIncrease(world, mutate);
                    adjustments.put(world, adjustment);
                    if (adjustment == Adjustment.INCREASE) {
                        totalAdditionalChunks += additionalChunks;
                    }
                }
            } else if (mspt >= decreaseMsptThreshold) {
                adjustments.put(world, tryDecrease(world, mutate));
            } else {
                adjustments.put(world, tryStay(world, mutate));
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


        public double getMaximumMsptPerChunk() {
            double max = 0;
            for (MsptChunkRecord record : records.values()) {
                max = Math.max(max, record.chunkCount > 0 ? (record.mspt / record.chunkCount) : 0);
            }
            return max;
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
