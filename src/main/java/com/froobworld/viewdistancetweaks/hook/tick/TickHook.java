package com.froobworld.viewdistancetweaks.hook.tick;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;

import java.util.function.Consumer;

public interface TickHook {

    void register(ViewDistanceTweaks viewDistanceTweaks);

    boolean addTickConsumer(Consumer<Long> consumer);

    boolean removeTickConsumer(Consumer<Long> consumer);

}
