package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ProactiveAdjustmentMode extends BaseAdjustmentMode {
    private final int globalChunkCountTarget;
    private final SimulationDistanceHook simulationDistanceHook;
    private final ChunkCounter chunkCounter;

    public ProactiveAdjustmentMode(int globalChunkCountTarget, SimulationDistanceHook simulationDistanceHook, ChunkCounter chunkCounter, Function<World, Boolean> exclude, Function<World, Integer> maxViewDistance,
                                   Function<World, Integer> minViewDistance, int requiredForIncrease, int requiredForDecrease) {
        super(simulationDistanceHook, exclude, maxViewDistance, minViewDistance, requiredForIncrease, requiredForDecrease);
        this.globalChunkCountTarget = globalChunkCountTarget;
        this.simulationDistanceHook = simulationDistanceHook;
        this.chunkCounter = chunkCounter;
    }

    @Override
    public Map<World, Adjustment> getAdjustments(Collection<World> worlds, boolean mutate) {
        Map<World, Integer> chunkCounts = new HashMap<>();
        int totalCount = 0;
        for (World world : worlds) {
            totalCount += chunkCounts.computeIfAbsent(world, w -> (int) chunkCounter.countChunks(w, simulationDistanceHook.getDistance(world)));
        }

        Map<World, Adjustment> adjustments = new HashMap<>();
        for (World world : worlds) {
            int chunkCountDiff = 0;
            if (totalCount < globalChunkCountTarget) {
                int newChunkCount = (int) chunkCounter.countChunks(world, simulationDistanceHook.getDistance(world) + 1);
                int oldCount = chunkCounts.get(world);
                Adjustment adjustment = tryIncrease(world, mutate);

                if (totalCount + newChunkCount - oldCount <= globalChunkCountTarget) {
                    adjustments.put(world, adjustment);
                    if (adjustment == Adjustment.INCREASE) {
                        chunkCountDiff = newChunkCount - oldCount;
                    }
                } else {
                    adjustments.put(world, Adjustment.STAY);
                }
            } else if (totalCount > globalChunkCountTarget) {
                Adjustment adjustment = tryDecrease(world, mutate);
                adjustments.put(world, adjustment);
                if (adjustment == Adjustment.DECREASE) {
                    int newChunkCount = (int) chunkCounter.countChunks(world, simulationDistanceHook.getDistance(world) - 1);
                    int oldCount = chunkCounts.get(world);
                    chunkCountDiff = newChunkCount - oldCount;
                }
            } else {
                Adjustment adjustment = tryStay(world, mutate);
                adjustments.put(world, adjustment);
            }
            totalCount += chunkCountDiff;
        }

        return adjustments;
    }


}
