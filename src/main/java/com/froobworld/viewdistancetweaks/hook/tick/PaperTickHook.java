package com.froobworld.viewdistancetweaks.hook.tick;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class PaperTickHook implements TickHook {
    private final Set<Consumer<Long>> tickConsumers = new HashSet<>();
    private final Listener tickListener = new Listener() {
        private Long start = null;

        @EventHandler
        private void onTickStart(ServerTickStartEvent event) {
            start = System.nanoTime();
        }

        @EventHandler
        private void onTickEnd(ServerTickEndEvent event) {
            if (start != null) {
                long diff = System.nanoTime() - start;
                tickConsumers.forEach(consumer -> consumer.accept(diff));
                start = null;
            }
        }
    };

    @Override
    public void register(ViewDistanceTweaks viewDistanceTweaks) {
        Bukkit.getPluginManager().registerEvents(tickListener, viewDistanceTweaks);
    }

    @Override
    public boolean addTickConsumer(Consumer<Long> consumer) {
        return tickConsumers.add(consumer);
    }

    @Override
    public boolean removeTickConsumer(Consumer<Long> consumer) {
        return tickConsumers.remove(consumer);
    }

    public static boolean isCompatible() {
        try {
            Class.forName("com.destroystokyo.paper.event.server.ServerTickStartEvent");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

}
