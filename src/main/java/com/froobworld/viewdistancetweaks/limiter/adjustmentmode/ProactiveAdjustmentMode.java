package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ProactiveAdjustmentMode extends BaseAdjustmentMode {
    private final int globalChunkCountTarget;
    private final ViewDistanceHook viewDistanceHook;
    private final ChunkCounter chunkCounter;

    public ProactiveAdjustmentMode(int globalChunkCountTarget, ViewDistanceHook viewDistanceHook, ChunkCounter chunkCounter, Function<World, Integer> maxViewDistance,
                                   Function<World, Integer> minViewDistance, int requiredForIncrease, int requiredForDecrease) {
        super(viewDistanceHook, maxViewDistance, minViewDistance, requiredForIncrease, requiredForDecrease);
        this.globalChunkCountTarget = globalChunkCountTarget;
        this.viewDistanceHook = viewDistanceHook;
        this.chunkCounter = chunkCounter;
    }

    @Override
    public Map<World, Adjustment> getAdjustments(Collection<World> worlds) {
        Map<World, Integer> chunkCounts = new HashMap<>();
        int totalCount = 0;
        for (World world : worlds) {
            totalCount += chunkCounts.computeIfAbsent(world, w -> (int) chunkCounter.countChunks(w, viewDistanceHook.getViewDistance(world)));
        }

        Map<World, Adjustment> adjustments = new HashMap<>();
        for (World world : worlds) {
            int chunkCountDiff = 0;
            if (totalCount < globalChunkCountTarget) {
                int newChunkCount = (int) chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world) + 1);
                int oldCount = chunkCounts.get(world);
                Adjustment adjustment = tryIncrease(world);

                if (totalCount + newChunkCount - oldCount <= globalChunkCountTarget) {
                    adjustments.put(world, adjustment);
                    if (adjustment == Adjustment.INCREASE) {
                        chunkCountDiff = newChunkCount - oldCount;
                    }
                } else {
                    adjustments.put(world, Adjustment.STAY);
                }
            } else if (totalCount > globalChunkCountTarget) {
                Adjustment adjustment = tryDecrease(world);
                adjustments.put(world, adjustment);
                if (adjustment == Adjustment.DECREASE) {
                    int newChunkCount = (int) chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world) - 1);
                    int oldCount = chunkCounts.get(world);
                    chunkCountDiff = newChunkCount - oldCount;
                }
            } else {
                Adjustment adjustment = tryStay(world);
                adjustments.put(world, adjustment);
            }
            totalCount += chunkCountDiff;
        }

        return adjustments;
    }


}
