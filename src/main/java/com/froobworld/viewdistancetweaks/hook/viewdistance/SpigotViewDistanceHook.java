package com.froobworld.viewdistancetweaks.hook.viewdistance;

import com.froobworld.viewdistancetweaks.limiter.ClientViewDistanceManager;
import com.froobworld.viewdistancetweaks.util.NmsUtils;
import com.froobworld.viewdistancetweaks.util.SpigotViewDistanceSyncer;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static org.joor.Reflect.*;

public class SpigotViewDistanceHook implements ViewDistanceHook {
    private final ClientViewDistanceManager clientViewDistanceManager;

    public SpigotViewDistanceHook(ClientViewDistanceManager clientViewDistanceManager) {
        this.clientViewDistanceManager = clientViewDistanceManager;
    }

    @Override
    public int getDistance(World world) {
        int value = world.getViewDistance();
        if (NmsUtils.getMinorVersion() >= 20 && (NmsUtils.getMinorVersion() > 20 || NmsUtils.getRevisionNumber() > 1)) { // 1.20 R2 or higher
            value = value + 1;
        }
        return value;
    }

    @Override
    public void setDistance(World world, int value) {
        value = ViewDistanceUtils.clampViewDistance(value);
        if (value != getDistance(world)) {
            clientViewDistanceManager.preViewDistanceChange(world, value);
            on(world).call("getHandle")
                    .call("k")
                    .call("a", value);
            SpigotViewDistanceSyncer.syncSpigotViewDistances(world);
        }
    }

    private static void sendUpdatedSimulationDistance(World world, int distance) {
        Object packet = onClass(NmsUtils.getFullyQualifiedClassName("PacketPlayOutViewDistance", "network.protocol.game")).create(distance).get();

        for (Player player : world.getPlayers()) {
            on(player).call("getHandle")
                    .field("b")
                    .call("a", packet);
        }
    }

    public static boolean isCompatible() {
        return NmsUtils.getMajorVersion() == 1
                && NmsUtils.getMinorVersion() >= 18 // at least 1.18
                && NmsUtils.getMinorVersion() <= 20 // no more than 1.20
                && (NmsUtils.getRevisionNumber() < 20 || NmsUtils.getRevisionNumber() <= 2); // no more than 1.20 R2
    }

}
