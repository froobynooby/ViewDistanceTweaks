package com.froobworld.viewdistancetweaks.hook.tick;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;

import java.util.function.Consumer;

public interface TickHook {

    void register(ViewDistanceTweaks viewDistanceTweaks);

    boolean addTickCallback(Consumer<Integer> consumer);

    boolean removeTickCallback(Consumer<Integer> consumer);

}
