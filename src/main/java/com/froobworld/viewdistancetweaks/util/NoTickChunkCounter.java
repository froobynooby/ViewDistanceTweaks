package com.froobworld.viewdistancetweaks.util;

import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class NoTickChunkCounter implements ChunkCounter {
    private final SimulationDistanceHook simulationDistanceHook;
    private final Function<World, Double> chunkWeight;
    private final Function<World, Boolean> excludeOverlap;

    public NoTickChunkCounter(SimulationDistanceHook simulationDistanceHook, Function<World, Double> chunkWeight, Function<World, Boolean> excludeOverlap) {
        this.simulationDistanceHook = simulationDistanceHook;
        this.chunkWeight = chunkWeight;
        this.excludeOverlap = excludeOverlap;
    }


    @Override
    public double countChunks(World world, int noTickViewDistance) {
        double chunkWeight = this.chunkWeight.apply(world);
        boolean excludeOverlap = this.excludeOverlap.apply(world);
        int viewDistance = simulationDistanceHook.getDistance(world);
        int diff = noTickViewDistance - viewDistance;
        if (diff <= 0) {
            return 0;
        }

        int unweightedCount;
        if (excludeOverlap) {
            RectangleUnionAreaFinder areaFinder = new RectangleUnionAreaFinder();
            for (Player player : world.getPlayers()) {
                int chunkX = player.getLocation().getBlockX() >> 4;
                int chunkZ = player.getLocation().getBlockZ() >> 4;

                // 1 1 2 2 2 2 3 3
                // 1 1 2 2 2 2 3 3
                // 1 1 * * * * 3 3
                // 1 1 * * * * 3 3
                // 1 1 * * * * 3 3
                // 1 1 * * * * 3 3
                // 1 1 4 4 4 4 3 3
                // 1 1 4 4 4 4 3 3

                // 1
                areaFinder.addRect(
                        chunkX,
                        chunkZ,
                        chunkX + diff,
                        chunkZ + 2 * noTickViewDistance + 1
                );
                // 2
                areaFinder.addRect(
                        chunkX + diff,
                        chunkZ,
                        chunkX + 2 * noTickViewDistance + 1 - diff,
                        chunkZ + diff
                );
                // 3
                areaFinder.addRect(
                        chunkX + 2 * noTickViewDistance + 1 - diff,
                        chunkZ,
                        chunkX + 2 * noTickViewDistance + 1,
                        chunkZ + 2 * noTickViewDistance + 1
                );
                // 4
                areaFinder.addRect(
                        chunkX + diff,
                        chunkZ + 2 * noTickViewDistance + 1 - diff,
                        chunkX + 2 * noTickViewDistance + 1 - diff,
                        chunkZ + 2 * noTickViewDistance + 1
                );
            }
            unweightedCount = areaFinder.area();
        } else {
            unweightedCount = world.getPlayers().size() * ((int) Math.pow(2 * noTickViewDistance + 1, 2) - (int) Math.pow(2 * viewDistance + 1, 2));
        }
        //*
        return unweightedCount * chunkWeight;
    }

}
