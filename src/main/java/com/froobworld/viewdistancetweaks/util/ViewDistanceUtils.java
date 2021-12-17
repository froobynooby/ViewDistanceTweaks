package com.froobworld.viewdistancetweaks.util;

public final class ViewDistanceUtils {

    public static int clampViewDistance(int viewDistance) {
        return Math.min(32, Math.max(2, viewDistance));
    }

    public static int viewDistanceFromChunkCount(double chunkCount) {
        return (int) Math.floor((Math.sqrt(chunkCount) - 1) / 2.0);
    }

}
