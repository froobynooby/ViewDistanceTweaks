package com.froobworld.viewdistancetweaks.limiter;

import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import org.bukkit.World;

import java.util.Collection;
import java.util.function.Function;

public class ViewDistanceClamper {
    private final SimulationDistanceHook simulationDistanceHook;
    private final Function<World, Integer> maxViewDistance;
    private final Function<World, Integer> minViewDistance;

    public ViewDistanceClamper(SimulationDistanceHook simulationDistanceHook, Function<World, Integer> maxViewDistance, Function<World, Integer> minViewDistance) {
        this.simulationDistanceHook = simulationDistanceHook;
        this.maxViewDistance = maxViewDistance;
        this.minViewDistance = minViewDistance;
    }

    public void clampWorld(World world) {
        int viewDistance = simulationDistanceHook.getDistance(world);
        int newViewDistance = Math.max(Math.min(viewDistance, maxViewDistance.apply(world)), minViewDistance.apply(world));
        if (newViewDistance != viewDistance) {
            simulationDistanceHook.setDistance(world, newViewDistance);
        }
    }

    public void clampWorlds(Collection<World> worlds) {
        for (World world : worlds) {
            clampWorld(world);
        }
    }

}
