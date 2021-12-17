package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import static org.joor.Reflect.*;

public class ClientViewDistanceManager implements Listener {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public ClientViewDistanceManager(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        if (NmsUtils.getMajorVersion() >= 1 && NmsUtils.getMinorVersion() >= 18) {
            Bukkit.getOnlinePlayers().forEach(this::sendOptimisticViewDistance);
            Bukkit.getPluginManager().registerEvents(this, viewDistanceTweaks);
        }
    }

    public void preViewDistanceChange(World world, int newViewDistance) {
        int maxWorldViewDistance = viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get();
        world.getPlayers().forEach(player -> sendViewDistance(player, Math.max(maxWorldViewDistance, newViewDistance)));
    }

    private int getOptimisticViewDistance(World world) {
        int worldViewDistance = world.getViewDistance();
        int maxWorldViewDistance = viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get();
        return Math.max(worldViewDistance, maxWorldViewDistance);
    }

    private void sendOptimisticViewDistance(Player player) {
        sendViewDistance(player, getOptimisticViewDistance(player.getWorld()));
    }

    private void sendViewDistance(Player player, int viewDistance) {
        Object packet = onClass("net.minecraft.network.protocol.game.PacketPlayOutViewDistance").create(player.getWorld().getViewDistance()).create(viewDistance).get();
        on(player).call("getHandle")
                .field("b")
                .call("a", packet);
    }

    @EventHandler
    private void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        sendOptimisticViewDistance(event.getPlayer());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        sendOptimisticViewDistance(event.getPlayer());
    }

}
