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
    }

    public void init() {
        if (NmsUtils.getMajorVersion() >= 1 && NmsUtils.getMinorVersion() >= 18) {
            Bukkit.getOnlinePlayers().forEach(player -> sendOptimisticViewDistance(player, viewDistanceTweaks.getHookManager().getViewDistanceHook().getDistance(player.getWorld())));
            Bukkit.getPluginManager().registerEvents(this, viewDistanceTweaks);
            for (Player player : Bukkit.getOnlinePlayers()) {
                ViewDistancePacketModifier packetModifier = new ViewDistancePacketModifier(this, player);
                packetModifier.inject();
                packetModifierMap.put(player, packetModifier);
            }
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
        int modifier = viewDistanceTweaks.getHookManager().getViewDistanceHook() instanceof PaperViewDistanceHook ? 1 : 0;
        int maxWorldViewDistance = viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get() + modifier;
        return Math.max(worldViewDistance, maxWorldViewDistance) + modifier;
    }

    private void sendOptimisticViewDistance(Player player, int worldViewDistance) {
        sendViewDistance(player, getOptimisticSendDistance(player.getWorld(), worldViewDistance));
    }

    private void sendViewDistance(Player player, int viewDistance) {
        Object packet = onClass("net.minecraft.network.protocol.game.PacketPlayOutViewDistance").create(player.getWorld().getViewDistance()).create(viewDistance).get();
        on(player).call("getHandle")
                .field("b")
                .call("a", packet);
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
        private static final Class<?> viewDistancePacketClass = onClass("net.minecraft.network.protocol.game.PacketPlayOutViewDistance").type();

        private final ClientViewDistanceManager clientViewDistanceManager;
        private final Player player;

        private ViewDistancePacketModifier(ClientViewDistanceManager clientViewDistanceManager, Player player) {
            this.clientViewDistanceManager = clientViewDistanceManager;
            this.player = player;
        }

        public void inject() {
            if (NmsUtils.getMinorVersion() == 19) {
                on(player).call("getHandle")
                        .field("b")
                        .field("b")
                        .field("m")
                        .call("pipeline")
                        .call("addLast", "vdt_packet_handler", this);
            } else if (NmsUtils.getMinorVersion() == 18) {
                if (NmsUtils.getRevisionNumber() == 2) {
                    on(player).call("getHandle")
                            .field("b") // PlayerConnection
                            .field("a") // NetWorkManager
                            .field("m") // Channel
                            .call("pipeline")
                            .call("addLast", "vdt_packet_handler", this);

                } else if (NmsUtils.getRevisionNumber() == 1) {
                    on(player).call("getHandle")
                            .field("b") // PlayerConnection
                            .field("a") // NetWorkManager
                            .field("k") // Channel
                            .call("pipeline")
                            .call("addLast", "vdt_packet_handler", this);
                }
            }
        }

        public void remove() {
            try {
                if (NmsUtils.getMinorVersion() == 19) {
                    on(player).call("getHandle")
                            .field("b")
                            .field("b")
                            .field("m")
                            .call("pipeline")
                            .call("remove", "vdt_packet_handler");
                } else if (NmsUtils.getMinorVersion() == 18) {
                    if (NmsUtils.getRevisionNumber() == 2) {
                        if (NmsUtils.getRevisionNumber() == 2) {
                            on(player).call("getHandle")
                                    .field("b") // PlayerConnection
                                    .field("a") // NetWorkManager
                                    .field("m") // Channel
                                    .call("pipeline")
                                    .call("remove", "vdt_packet_handler");
                        } else if (NmsUtils.getRevisionNumber() == 1) {
                            on(player).call("getHandle")
                                    .field("b") // PlayerConnection
                                    .field("a") // NetWorkManager
                                    .field("k") // Channel
                                    .call("pipeline")
                                    .call("remove", "vdt_packet_handler");
                        }
                    }
                }
            } catch (Exception ignored) {} // Throws an exception if pipeline does not contain the handler, in which case work is already done
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg.getClass().equals(viewDistancePacketClass)) {
                int viewDistance = on(msg).get("a");
                int newViewDistance = clientViewDistanceManager.getOptimisticSendDistance(player.getWorld(), viewDistance);
                on(msg).set("a", newViewDistance);
            }
            super.write(ctx, msg, promise);
        }
    }

}
