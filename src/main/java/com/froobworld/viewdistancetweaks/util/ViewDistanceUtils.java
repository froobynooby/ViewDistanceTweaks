package com.froobworld.viewdistancetweaks.util;

import org.bukkit.Bukkit;
import org.bukkit.World;

import static org.joor.Reflect.*;

public final class ViewDistanceUtils {

    public static int clampViewDistance(int viewDistance) {
        return Math.min(32, Math.max(2, viewDistance));
    }

    public static int viewDistanceFromChunkCount(double chunkCount) {
        return (int) Math.floor((Math.sqrt(chunkCount) - 1) / 2.0);
    }

    public static void syncSpigotViewDistances(World world) {
        on(world).call("getHandle")
                .field("spigotConfig")
                .set("viewDistance", getProperViewDistance(world));
    }

    public static void syncSpigotViewDistances() {
        for (World world : Bukkit.getWorlds()) {
            syncSpigotViewDistances(world);
        }
    }

    private static int getProperViewDistance(World world) {
        int viewDistancePlusOne = on(world).call("getHandle")
                .call("getChunkProvider")
                .field("playerChunkMap")
                .field("viewDistance").get();
        return viewDistancePlusOne - 1;
    }

}
