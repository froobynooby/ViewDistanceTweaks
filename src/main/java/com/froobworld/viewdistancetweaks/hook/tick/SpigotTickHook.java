package com.froobworld.viewdistancetweaks.hook.tick;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.joor.Reflect.*;

public class SpigotTickHook implements TickHook {
    private static final long[] tickTimes;
    static {
        tickTimes = ((CraftServer) Bukkit.getServer()).getServer().getTickTimesNanos();
    }

    private final Set<Consumer<Long>> tickConsumers = new HashSet<>();
    private Integer taskId;

    @Override
    public void register(ViewDistanceTweaks viewDistanceTweaks) {
        if (taskId == null) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    viewDistanceTweaks,
                    () -> tickConsumers.forEach(consumer -> consumer.accept(getLastTickTime())),
                    0, 1
            );
        }
    }

    @Override
    public boolean addTickConsumer(Consumer<Long> consumer) {
        return tickConsumers.add(consumer);
    }

    @Override
    public boolean removeTickConsumer(Consumer<Long> consumer) {
        return tickConsumers.remove(consumer);
    }

    private long getLastTickTime() {
        return tickTimes == null ? 0 : tickTimes[(getCurrentTick() - 1) % 100];
    }

    private static int getCurrentTick() {
        return on(Bukkit.getScheduler())
                .get("currentTick");
    }

}
