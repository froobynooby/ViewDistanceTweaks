package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.PaperViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.NmsUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

import static org.joor.Reflect.*;

public class ClientViewDistanceManager implements Listener {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private final Map<Player, ViewDistancePacketModifier> packetModifierMap = new HashMap<>();

    public ClientViewDistanceManager(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        if (NmsUtils.getMajorVersion() >= 1 && NmsUtils.getMinorVersion() >= 18) {
            Bukkit.getOnlinePlayers().forEach(this::sendOptimisticViewDistance);
            Bukkit.getPluginManager().registerEvents(this, viewDistanceTweaks);
            start();
        }
    }

    private void start() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ViewDistancePacketModifier packetModifier = new ViewDistancePacketModifier(this, player);
            packetModifier.inject();
            packetModifierMap.put(player, packetModifier);
        }
    }

    public void shutdown() {
        packetModifierMap.values().forEach(ViewDistancePacketModifier::remove);
        packetModifierMap.clear();
    }

    public void preViewDistanceChange(World world, int newViewDistance) {
        int maxWorldViewDistance = viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get();
        world.getPlayers().forEach(player -> sendViewDistance(player, Math.max(maxWorldViewDistance, newViewDistance)));
    }

    private int getOptimisticSendDistance(World world) {
        int modifier = viewDistanceTweaks.getHookManager().getViewDistanceHook() instanceof PaperViewDistanceHook ? 1 : 0;
        int worldViewDistance = world.getViewDistance() + modifier;
        int maxWorldViewDistance = viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get() + modifier;
        return Math.max(worldViewDistance, maxWorldViewDistance);
    }

    private void sendOptimisticViewDistance(Player player) {
        sendViewDistance(player, getOptimisticSendDistance(player.getWorld()));
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
        ViewDistancePacketModifier packetModifier = new ViewDistancePacketModifier(this, event.getPlayer());
        packetModifier.inject();
        packetModifierMap.put(event.getPlayer(), packetModifier);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        packetModifierMap.remove(event.getPlayer());
    }

    private static class ViewDistancePacketModifier extends ChannelDuplexHandler {
        private static final Class<?> viewDistancePacketClass = onClass("net.minecraft.network.protocol.game.PacketPlayOutViewDistance").type();

        private final ClientViewDistanceManager clientViewDistanceManager;
        private final Player player;

        private ViewDistancePacketModifier(ClientViewDistanceManager clientViewDistanceManager, Player player) {
            this.clientViewDistanceManager = clientViewDistanceManager;
            this.player = player;
        }

        public void inject() {
            on(player).call("getHandle")
                    .field("b") // PlayerConnection
                    .field("a") // NetWorkManager
                    .field("k") // Channel
                    .call("pipeline")
                    .call("addLast", "vdt_packet_handler", this);
        }

        public void remove() {
            on(player).call("getHandle")
                    .field("b") // PlayerConnection
                    .field("a") // NetWorkManager
                    .field("k") // Channel
                    .call("pipeline")
                    .call("remove", "vdt_packet_handler");
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg.getClass().equals(viewDistancePacketClass)) {
                int viewDistance = on(msg).get("a");
                int newViewDistance = Math.max(viewDistance, clientViewDistanceManager.getOptimisticSendDistance(player.getWorld()));
                on(msg).set("a", newViewDistance);
            }
            super.write(ctx, msg, promise);
        }
    }

}
