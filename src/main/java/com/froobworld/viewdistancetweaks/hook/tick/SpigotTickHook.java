package com.froobworld.viewdistancetweaks.hook.tick;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.NmsUtils;
import org.bukkit.Bukkit;
import org.joor.Reflect;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.joor.Reflect.*;

public class SpigotTickHook implements TickHook {
    private static final Reflect currentTick = onClass(NmsUtils.getFullyQualifiedClassName("MinecraftServer")).field("currentTick");

    private final Set<Consumer<Integer>> tickCallbacks = new HashSet<>();
    private Integer taskId;

    @Override
    public void register(ViewDistanceTweaks viewDistanceTweaks) {
        if (taskId == null) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    viewDistanceTweaks,
                    () -> tickCallback(getCurrentTick()),
                    0, 1
            );
        }
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

    private static int getCurrentTick() {
        return currentTick.get();
    }

}
