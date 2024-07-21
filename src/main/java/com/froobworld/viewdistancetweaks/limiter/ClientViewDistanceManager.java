package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientViewDistanceManager implements Listener {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private final Map<Player, ViewDistancePacketModifier> packetModifierMap = new HashMap<>();

    public ClientViewDistanceManager(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    public void init() {
        Bukkit.getOnlinePlayers().forEach(player -> sendOptimisticViewDistance(player, viewDistanceTweaks.getHookManager().getViewDistanceHook().getDistance(player.getWorld())));
        Bukkit.getPluginManager().registerEvents(this, viewDistanceTweaks);
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
        world.getPlayers().forEach(player -> sendOptimisticViewDistance(player, newViewDistance));
    }

    private int getOptimisticSendDistance(World world, int worldViewDistance) {
        int maxWorldViewDistance = viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get();
        return Math.max(worldViewDistance, maxWorldViewDistance);
    }

    private void sendOptimisticViewDistance(Player player, int worldViewDistance) {
        sendViewDistance(player, getOptimisticSendDistance(player.getWorld(), worldViewDistance));
    }

    private void sendViewDistance(Player player, int viewDistance) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(new ClientboundSetChunkCacheRadiusPacket(viewDistance));
    }

    @EventHandler
    private void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        sendOptimisticViewDistance(event.getPlayer(), viewDistanceTweaks.getHookManager().getViewDistanceHook().getDistance(event.getPlayer().getWorld()));
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        sendOptimisticViewDistance(event.getPlayer(), viewDistanceTweaks.getHookManager().getViewDistanceHook().getDistance(event.getPlayer().getWorld()));
        ViewDistancePacketModifier packetModifier = new ViewDistancePacketModifier(this, event.getPlayer());
        packetModifier.inject();
        packetModifierMap.put(event.getPlayer(), packetModifier);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        packetModifierMap.remove(event.getPlayer());
    }

    private static class ViewDistancePacketModifier extends ChannelDuplexHandler {

        private final ClientViewDistanceManager clientViewDistanceManager;
        private final Player player;

        private ViewDistancePacketModifier(ClientViewDistanceManager clientViewDistanceManager, Player player) {
            this.clientViewDistanceManager = clientViewDistanceManager;
            this.player = player;
        }

        public void inject() {
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            nmsPlayer.connection.connection.channel.pipeline().addLast("vdt_packet_handler", this);
        }

        public void remove() {
            try {
                ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
                nmsPlayer.connection.connection.channel.pipeline().remove("vdt_packet_handler");
            } catch (Exception ignored) {} // Throws an exception if pipeline does not contain the handler, in which case work is already done
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof ClientboundSetChunkCacheRadiusPacket) {
                int viewDistance = ((ClientboundSetChunkCacheRadiusPacket) msg).getRadius();
                int newViewDistance = clientViewDistanceManager.getOptimisticSendDistance(player.getWorld(), viewDistance);
                if (viewDistance != newViewDistance) {
                    msg = new ClientboundSetChunkCacheRadiusPacket(newViewDistance);
                }
            }
            super.write(ctx, msg, promise);
        }
    }

}
