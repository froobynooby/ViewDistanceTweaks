package com.froobworld.viewdistancetweaks.util;

import org.bukkit.World;
import org.bukkit.entity.Player;

import static org.joor.Reflect.*;

public final class ViewDistanceUtils {

    public static void setViewDistance(World world, int viewDistance) {
        viewDistance = clampViewDistance(viewDistance);
        if (viewDistance == world.getViewDistance()) return;

        on(world).call("getHandle")
                .field("spigotConfig")
                .set("viewDistance", viewDistance);

        sendUpdatedViewDistance(world, viewDistance);

        on(world).call("getHandle")
                .call("getChunkProvider")
                .call("setViewDistance", viewDistance);
    }

    private static void sendUpdatedViewDistance(World world, int viewDistance) {
        Object packet = on(NmsUtils.getFullyQualifiedClassName("PacketPlayOutViewDistance")).create(viewDistance).get();

        for (Player player : world.getPlayers()) {
            on(player).call("getHandle")
                    .field("playerConnection")
                    .call("sendPacket", packet);
        }
    }

    public static int clampViewDistance(int viewDistance) {
        return Math.min(32, Math.max(2, viewDistance));
    }

    public static int viewDistanceFromChunkCount(double chunkCount) {
        return (int) Math.floor((Math.sqrt(chunkCount) - 1) / 2.0);
    }

}
