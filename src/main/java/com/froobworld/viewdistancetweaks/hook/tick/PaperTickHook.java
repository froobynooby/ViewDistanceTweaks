package com.froobworld.viewdistancetweaks.hook.tick;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class PaperTickHook implements TickHook {
    private final Set<Consumer<Integer>> tickCallbacks = new HashSet<>();
    private final Listener tickListener = new Listener() {
        @EventHandler
        public void onTickStart(ServerTickStartEvent event) {
            tickCallback(event.getTickNumber());
        }
    };


    @Override
    public void register(ViewDistanceTweaks viewDistanceTweaks) {
        Bukkit.getPluginManager().registerEvents(tickListener, viewDistanceTweaks);
    }

    @Override
    public boolean addTickCallback(Consumer<Integer> consumer) {
        return tickCallbacks.add(consumer);
    }

    @Override
    public boolean removeTickCallback(Consumer<Integer> consumer) {
        return tickCallbacks.remove(consumer);
    }

    private void tickCallback(int tickNumber) {
        tickCallbacks.forEach(consumer -> consumer.accept(tickNumber));
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
