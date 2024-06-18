package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import org.bukkit.World;

import java.util.Collection;
import java.util.Map;

public class AdjustmentModes {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private AdjustmentMode sdAdjustmentMode;
    private AdjustmentMode vdAdjustmentMode;

    public AdjustmentModes(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    public AdjustmentMode getSimulationDistanceAdjustmentMode() {
        return sdAdjustmentMode;
    }

    public AdjustmentMode getViewDistanceAdjustmentMode() {
        return vdAdjustmentMode;
    }

    public void init() {
        AdjustmentMode.Mode mode = viewDistanceTweaks.getVdtConfig().adjustmentMode.get();

        AdjustmentMode sdProactiveAdjustmentMode = null;
        AdjustmentMode sdReactiveAdjustmentMode = null;
        if (mode == AdjustmentMode.Mode.REACTIVE || mode == AdjustmentMode.Mode.MIXED) {
            sdReactiveAdjustmentMode = new ReactiveAdjustmentMode(
                    viewDistanceTweaks.getTaskManager().getMsptTracker(),
                    viewDistanceTweaks.getHookManager().getChunkCounter(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.increaseMsptThreshold.get(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.decreaseMsptThreshold.get(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.msptPrediction.historyLength.get(),
                    viewDistanceTweaks.getVdtConfig().reactiveMode.msptPrediction.enabled.get(),
                    viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.exclude.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
        }
        if (mode == AdjustmentMode.Mode.PROACTIVE || mode == AdjustmentMode.Mode.MIXED) {
            sdProactiveAdjustmentMode = new ProactiveAdjustmentMode(
                    viewDistanceTweaks.getVdtConfig().proactiveMode.globalTickingChunkCountTarget.get(),
                    viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                    viewDistanceTweaks.getHookManager().getChunkCounter(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.exclude.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get(),
                    world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                    viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
            );
        }

        AdjustmentMode vdProactiveAdjustmentMode = null;
        AdjustmentMode vdReactiveAdjustmentMode = null;
        if (viewDistanceTweaks.getHookManager().getViewDistanceHook() != null) {
            if ((mode == AdjustmentMode.Mode.REACTIVE || mode == AdjustmentMode.Mode.MIXED) && viewDistanceTweaks.getVdtConfig().reactiveMode.reactiveViewDistance.useReactiveViewDistance.get()) {
                vdReactiveAdjustmentMode = new ReactiveAdjustmentMode(
                        viewDistanceTweaks.getTaskManager().getMsptTracker(),
                        viewDistanceTweaks.getHookManager().getNoTickChunkCounter(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.increaseMsptThreshold.get(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.decreaseMsptThreshold.get(),
                        viewDistanceTweaks.getVdtConfig().reactiveMode.msptPrediction.historyLength.get(),
                        false, // no MSPT prediction for view distance
                        viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.exclude.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.minimumViewDistance.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
                );
            }
            if (mode == AdjustmentMode.Mode.PROACTIVE || mode == AdjustmentMode.Mode.MIXED || !viewDistanceTweaks.getVdtConfig().reactiveMode.reactiveViewDistance.useReactiveViewDistance.get()) {
                vdProactiveAdjustmentMode = new ProactiveAdjustmentMode(
                        viewDistanceTweaks.getVdtConfig().proactiveMode.globalNonTickingChunkCountTarget.get(),
                        viewDistanceTweaks.getHookManager().getViewDistanceHook(),
                        viewDistanceTweaks.getHookManager().getNoTickChunkCounter(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.exclude.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get(),
                        world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.minimumViewDistance.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForIncrease.get(),
                        viewDistanceTweaks.getVdtConfig().passedChecksForDecrease.get()
                );
            }
        }
        if (sdReactiveAdjustmentMode != null && vdReactiveAdjustmentMode != null) {
            double ratio = Math.max(1, viewDistanceTweaks.getVdtConfig().reactiveMode.reactiveViewDistance.targetViewDistanceRatio.get());
            AdjustmentMode newSdReactiveAdjustmentMode = new RatioPreservingAdjustmentMode(
                    ratio,
                    RatioPreservingAdjustmentMode.Type.SIMULATION_DISTANCE,
                    sdReactiveAdjustmentMode,
                    vdReactiveAdjustmentMode,
                    viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                    viewDistanceTweaks.getHookManager().getViewDistanceHook()
            );
            AdjustmentMode newVdReactiveAdjustmentMode = new RatioPreservingAdjustmentMode(
                    ratio,
                    RatioPreservingAdjustmentMode.Type.VIEW_DISTANCE,
                    sdReactiveAdjustmentMode,
                    vdReactiveAdjustmentMode,
                    viewDistanceTweaks.getHookManager().getSimulationDistanceHook(),
                    viewDistanceTweaks.getHookManager().getViewDistanceHook()
            );
            sdReactiveAdjustmentMode = newSdReactiveAdjustmentMode;
            vdReactiveAdjustmentMode = newVdReactiveAdjustmentMode;
        }
        this.sdAdjustmentMode = sdProactiveAdjustmentMode == null ? sdReactiveAdjustmentMode
                : (sdReactiveAdjustmentMode == null ? sdProactiveAdjustmentMode
                : new MixedAdjustmentMode(sdProactiveAdjustmentMode, sdReactiveAdjustmentMode, AdjustmentMode.Adjustment::strongest));

        this.vdAdjustmentMode = vdProactiveAdjustmentMode == null ? vdReactiveAdjustmentMode
                : (vdReactiveAdjustmentMode == null ? vdProactiveAdjustmentMode
                : new MixedAdjustmentMode(vdProactiveAdjustmentMode, vdReactiveAdjustmentMode, AdjustmentMode.Adjustment::strongest));
    }


    private static class RatioPreservingAdjustmentMode implements AdjustmentMode {
        private final double targetRatio;
        private final Type type;
        private final AdjustmentMode simulationDistanceAdjustmentMode;
        private final AdjustmentMode viewDistanceAdjustmentMode;
        private final SimulationDistanceHook simulationDistanceHook;
        private final ViewDistanceHook viewDistanceHook;

        private RatioPreservingAdjustmentMode(double targetRatio, Type type, AdjustmentMode simulationDistanceAdjustmentMode, AdjustmentMode viewDistanceAdjustmentMode,SimulationDistanceHook simulationDistanceHook, ViewDistanceHook viewDistanceHook) {
            this.targetRatio = targetRatio;
            this.type = type;
            this.simulationDistanceAdjustmentMode = simulationDistanceAdjustmentMode;
            this.viewDistanceAdjustmentMode = viewDistanceAdjustmentMode;
            this.simulationDistanceHook = simulationDistanceHook;
            this.viewDistanceHook = viewDistanceHook;
        }

        @Override
        public Map<World, Adjustment> getAdjustments(Collection<World> worlds, boolean mutate) {
            if (type == Type.SIMULATION_DISTANCE) {
                Map<World, Adjustment> adjustmentMap = simulationDistanceAdjustmentMode.getAdjustments(worlds, true);
                Map<World, Adjustment> viewDistanceAdjustmentMap = viewDistanceAdjustmentMode.getAdjustments(worlds, false);
                for (World world : worlds) {
                    if (adjustmentMap.get(world) == Adjustment.INCREASE) {
                        // cancel the increase if it would put us outside the ratio, unless the view distance is being held up
                        if ((simulationDistanceHook.getDistance(world) + 1) * targetRatio > viewDistanceHook.getDistance(world)) {
                            if (viewDistanceAdjustmentMap.get(world) == Adjustment.INCREASE) {
                                adjustmentMap.put(world, Adjustment.STAY);
                            }
                        }
                    }
                }
                return adjustmentMap;
            } else {
                Map<World, Adjustment> adjustmentMap = viewDistanceAdjustmentMode.getAdjustments(worlds, true);
                Map<World, Adjustment> simulationDistanceAdjustmentMap = simulationDistanceAdjustmentMode.getAdjustments(worlds, false);
                for (World world : worlds) {
                    if (adjustmentMap.get(world) == Adjustment.DECREASE) {
                        if (simulationDistanceHook.getDistance(world) * targetRatio > viewDistanceHook.getDistance(world) - 1) {
                            if (simulationDistanceAdjustmentMap.get(world) == Adjustment.DECREASE) {
                                adjustmentMap.put(world, Adjustment.STAY);
                            }
                        }
                    }
                }
                return adjustmentMap;
            }
        }

        enum Type {
            VIEW_DISTANCE,
            SIMULATION_DISTANCE
        }

    }

}
