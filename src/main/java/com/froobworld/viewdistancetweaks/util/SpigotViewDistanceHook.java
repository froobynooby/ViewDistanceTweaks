package com.froobworld.viewdistancetweaks.util;

import org.bukkit.World;
import org.bukkit.entity.Player;

import static org.joor.Reflect.*;

public class SpigotViewDistanceHook implements ViewDistanceHook {

    @Override
    public int getViewDistance(World world) {
        return world.getViewDistance();
    }

    @Override
    public void setViewDistance(World world, int value) {
        value = ViewDistanceUtils.clampViewDistance(value);
        if (value != getViewDistance(world)) {
            sendUpdatedViewDistance(world, value);
            on(world).call("getHandle")
                    .call("getChunkProvider")
                    .call("setViewDistance", value);
            ViewDistanceUtils.syncSpigotViewDistances(world);
        }
    }

    private static void sendUpdatedViewDistance(World world, int viewDistance) {
        Object packet = on(NmsUtils.getFullyQualifiedClassName("PacketPlayOutViewDistance")).create(viewDistance).get();

        for (Player player : world.getPlayers()) {
            on(player).call("getHandle")
                    .field("playerConnection")
                    .call("sendPacket", packet);
        }
    }

}
