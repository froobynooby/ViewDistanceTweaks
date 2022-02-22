package com.froobworld.viewdistancetweaks.hook.tick;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.NmsUtils;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.joor.Reflect.*;

public class SpigotTickHook implements TickHook {
    private static final long[] tickTimes;
    static {
        Class<?> serverClass = null;
        try {
            serverClass = Class.forName(NmsUtils.getFullyQualifiedClassName("MinecraftServer", "server"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (serverClass == null) {
            tickTimes = null;
        } else {
            String fieldName = null;
            for (Field field : serverClass.getFields()) {
                if (field.getType().equals(long[].class)) {
                    fieldName = field.getName();
                }
            }
            tickTimes = on(Bukkit.getServer())
                    .call("getServer")
                    .get(fieldName);
        }
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
        return tickConsumers.add(consumer);
    }

    private long getLastTickTime() {
        return tickTimes == null ? 0 : tickTimes[(getCurrentTick() - 1) % 100];
    }

    private static int getCurrentTick() {
        return on(Bukkit.getScheduler())
                .get("currentTick");
    }

}
