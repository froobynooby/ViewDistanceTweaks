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
    private final Set<Consumer<Integer>> tickStartCallbacks = new HashSet<>();
    private final Set<Consumer<Integer>> tickEndCallbacks = new HashSet<>();
    private final Listener tickListener = new Listener() {
        @EventHandler
        public void onTickStart(ServerTickStartEvent event) {
            tickStartCallback(event.getTickNumber());
        }

        @EventHandler
        public void onTickEnd(ServerTickEndEvent event) {
            tickEndCallback(event.getTickNumber());
        }
    };


    @Override
    public void register(ViewDistanceTweaks viewDistanceTweaks) {
        Bukkit.getPluginManager().registerEvents(tickListener, viewDistanceTweaks);
    }

    @Override
    public boolean addTickStartCallback(Consumer<Integer> consumer) {
        return tickStartCallbacks.add(consumer);
    }

    @Override
    public boolean removeTickStartCallback(Consumer<Integer> consumer) {
        return tickStartCallbacks.remove(consumer);
    }

    @Override
    public boolean addTickEndCallback(Consumer<Integer> consumer) {
        return tickEndCallbacks.add(consumer);
    }

    @Override
    public boolean removeTickEndCallback(Consumer<Integer> consumer) {
        return tickEndCallbacks.add(consumer);
    }

    private void tickStartCallback(int tickNumber) {
        tickStartCallbacks.forEach(consumer -> consumer.accept(tickNumber));
    }

    private void tickEndCallback(int tickNumber) {
        tickEndCallbacks.forEach(consumer -> consumer.accept(tickNumber));
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
