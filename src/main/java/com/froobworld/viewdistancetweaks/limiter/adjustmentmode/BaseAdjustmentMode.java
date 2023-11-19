package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class BaseAdjustmentMode implements AdjustmentMode {
    private final Map<UUID, AdjustmentHistory> worldAdjustmentHistory = new HashMap<>();
    private final SimulationDistanceHook simulationDistanceHook;
    private final Function<World, Boolean> exclude;
    private final Function<World, Integer> maximumViewDistance;
    private final Function<World, Integer> minimumViewDistance;
    private final int requiredIncrease;
    private final int requiredDecrease;

    public BaseAdjustmentMode(SimulationDistanceHook simulationDistanceHook, Function<World,Boolean> exclude, Function<World, Integer> maximumViewDistance, Function<World, Integer> minimumViewDistance,
                              int requiredIncrease, int requiredDecrease) {
        this.simulationDistanceHook = simulationDistanceHook;
        this.exclude = exclude;
        this.maximumViewDistance = maximumViewDistance;
        this.minimumViewDistance = minimumViewDistance;
        this.requiredIncrease = requiredIncrease;
        this.requiredDecrease = requiredDecrease;
    }

    protected Adjustment tryIncrease(World world, boolean mutate) {
        if (exclude.apply(world)) {
            return Adjustment.STAY;
        }
        return getAdjustmentHistory(world).increase(mutate) < requiredIncrease ? Adjustment.STAY :
                simulationDistanceHook.getDistance(world) < maximumViewDistance.apply(world) ? Adjustment.INCREASE : Adjustment.STAY;
    }

    protected Adjustment tryDecrease(World world, boolean mutate) {
        if (exclude.apply(world)) {
            return Adjustment.STAY;
        }
        return getAdjustmentHistory(world).decrease(mutate) < requiredDecrease ? Adjustment.STAY :
                simulationDistanceHook.getDistance(world) > minimumViewDistance.apply(world) ? Adjustment.DECREASE : Adjustment.STAY;
    }

    protected Adjustment tryStay(World world, boolean mutate) {
        getAdjustmentHistory(world).stay(mutate);
        return Adjustment.STAY;
    }

    private AdjustmentHistory getAdjustmentHistory(World world) {
        return worldAdjustmentHistory.computeIfAbsent(world.getUID(), uuid -> new AdjustmentHistory());
    }

    private static class AdjustmentHistory {
        private int increaseCount;
        private int decreaseCount;

        public int increase(boolean mutate) {
            if (!mutate) {
                return increaseCount + 1;
            }
            this.increaseCount++;
            this.decreaseCount = 0;
            return increaseCount;
        }

        public int decrease(boolean mutate) {
            if (!mutate) {
                return increaseCount + 1;
            }
            this.decreaseCount++;
            this.increaseCount = 0;
            return decreaseCount;
        }

        public void stay(boolean mutate) {
            if (!mutate) {
                return;
            }
            this.increaseCount = 0;
            this.decreaseCount = 0;
        }

    }

}
