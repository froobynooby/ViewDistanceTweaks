package com.froobworld.viewdistancetweaks.hook.tick;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;

import java.util.function.Consumer;

public interface TickHook {

    void register(ViewDistanceTweaks viewDistanceTweaks);

    boolean addTickStartCallback(Consumer<Integer> consumer);

    boolean removeTickStartCallback(Consumer<Integer> consumer);

    boolean addTickEndCallback(Consumer<Integer> consumer);

    boolean removeTickEndCallback(Consumer<Integer> consumer);

}
