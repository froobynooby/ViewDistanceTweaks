package com.froobworld.viewdistancetweaks.util;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class StandardChunkCounter implements ChunkCounter {
    private final Function<World, Double> chunkWeight;
    private final Function<World, Boolean> excludeOverlap;

    public StandardChunkCounter(Function<World, Double> chunkWeight, Function<World, Boolean> excludeOverlap) {
        this.chunkWeight = chunkWeight;
        this.excludeOverlap = excludeOverlap;
    }


    @Override
    public double countChunks(World world, int viewDistance) {
        double chunkWeight = this.chunkWeight.apply(world);
        boolean excludeOverlap = this.excludeOverlap.apply(world);

        int unweightedCount;
        if (excludeOverlap) {
            RectangleUnionAreaFinder areaFinder = new RectangleUnionAreaFinder();
            for (Player player : world.getPlayers()) {
                // We don't actually care about getting the coordinates of the rectangles right, they just all need to be shifted by the same amount relative to the player
                areaFinder.addRect(
                        player.getLocation().getBlockX() >> 4,
                        player.getLocation().getBlockZ() >> 4,
                        (player.getLocation().getBlockX() >> 4) + 2 * viewDistance + 1,
                        (player.getLocation().getBlockZ() >> 4) + 2 * viewDistance + 1
                );
            }
            unweightedCount = areaFinder.area();
        } else {
            unweightedCount = world.getPlayers().size() * (int) Math.pow(2 * viewDistance + 1, 2);
        }

        return unweightedCount * chunkWeight;
    }

}
