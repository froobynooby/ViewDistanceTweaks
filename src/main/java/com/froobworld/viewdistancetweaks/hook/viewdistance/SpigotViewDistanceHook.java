package com.froobworld.viewdistancetweaks.hook.viewdistance;

import com.froobworld.viewdistancetweaks.util.NmsUtils;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
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
        Object packet = onClass(NmsUtils.getFullyQualifiedClassName("PacketPlayOutViewDistance", "network.protocol.game")).create(viewDistance).get();

        for (Player player : world.getPlayers()) {
            on(player).call("getHandle")
                    .field(NmsUtils.getFieldOrMethodName("playerConnection", "b"))
                    .call("sendPacket", packet);
        }
    }

}
