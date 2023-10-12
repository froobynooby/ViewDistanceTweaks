package com.froobworld.viewdistancetweaks.hook.viewdistance;

import com.froobworld.viewdistancetweaks.util.NmsUtils;
import com.froobworld.viewdistancetweaks.util.SpigotViewDistanceSyncer;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static org.joor.Reflect.*;

public class SpigotSimulationDistanceHook implements SimulationDistanceHook {

    @Override
    public int getDistance(World world) {
        if (NmsUtils.getMinorVersion() >= 18) {
            return on(world)
                    .call("getSimulationDistance")
                    .get();
        } else {
            return world.getViewDistance();
        }
    }

    @Override
    public void setDistance(World world, int value) {
        value = ViewDistanceUtils.clampViewDistance(value);
        if (value != getDistance(world)) {
            sendUpdatedSimulationDistance(world, value);
            if (NmsUtils.getMinorVersion() >= 18 && NmsUtils.getMinorVersion() <= 20) {
                on(world).call("getHandle")
                        .call("k")
                        .call("b", value);
            } else if (NmsUtils.getMinorVersion() < 18) {
                on(world).call("getHandle")
                        .call("getChunkProvider")
                        .call("setViewDistance", value);
            }
            SpigotViewDistanceSyncer.syncSpigotViewDistances(world);
        }
    }

    private static void sendUpdatedSimulationDistance(World world, int distance) {
        Object packet;
        if (NmsUtils.getMinorVersion() < 18) {
            packet = onClass(NmsUtils.getFullyQualifiedClassName("PacketPlayOutViewDistance", "network.protocol.game")).create(distance).get();
        } else {
            packet = onClass(NmsUtils.getFullyQualifiedClassName("ClientboundSetSimulationDistancePacket", "network.protocol.game")).create(distance).get();
        }

        for (Player player : world.getPlayers()) {
            if (NmsUtils.getMinorVersion() == 20) {
                on(player).call("getHandle")
                        .field("c")
                        .call("a", packet);
            } else if (NmsUtils.getMinorVersion() == 18 || NmsUtils.getMinorVersion() == 19) {
                on(player).call("getHandle")
                        .field("b")
                        .call("a", packet);
            } else if (NmsUtils.getMinorVersion() == 17) {
                on(player).call("getHandle")
                        .field("b")
                        .call("sendPacket", packet);
            } else if (NmsUtils.getMinorVersion() < 17) {
                on(player).call("getHandle")
                        .field("playerConnection")
                        .call("sendPacket", packet);
            }
        }
    }

    public static boolean isCompatible() {
        return NmsUtils.getMajorVersion() == 1
                && NmsUtils.getMinorVersion() <= 20 // no more than 1.20
                && (NmsUtils.getRevisionNumber() < 2 || NmsUtils.getRevisionNumber() <= 2); // no more than 1.20 R1
    }

}
