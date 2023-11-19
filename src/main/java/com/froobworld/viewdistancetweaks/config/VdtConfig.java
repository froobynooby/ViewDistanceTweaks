package com.froobworld.viewdistancetweaks.config;

import com.froobworld.nabconfiguration.*;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.limiter.adjustmentmode.AdjustmentMode;
import org.bukkit.World;

import java.io.File;

public class VdtConfig extends NabConfiguration {
    public static final int VERSION = 9;

    public VdtConfig(ViewDistanceTweaks viewDistanceTweaks) {
        super(
                new File(viewDistanceTweaks.getDataFolder(), "config.yml"),
                () -> viewDistanceTweaks.getResource("resources/config.yml"),
                version -> viewDistanceTweaks.getResource("resources/config-patches/" + version + ".patch"),
                VERSION
        );
    }

    @Entry(key = "enabled")
    public final ConfigEntry<Boolean> enabled = new ConfigEntry<>();

    @Entry(key = "adjustment-mode")
    public final ConfigEntry<AdjustmentMode.Mode> adjustmentMode = ConfigEntries.enumEntry(AdjustmentMode.Mode.class);

    @Section(key = "proactive-mode-settings")
    public final ProactiveModeSettings proactiveMode = new ProactiveModeSettings();

    public static class ProactiveModeSettings extends ConfigSection {

        @Entry(key = "global-ticking-chunk-count-target")
        public final ConfigEntry<Integer> globalTickingChunkCountTarget = ConfigEntries.integerEntry();

        @Entry(key = "global-non-ticking-chunk-count-target")
        public final ConfigEntry<Integer> globalNonTickingChunkCountTarget = new ConfigEntry<>();
    }

    @Section(key = "reactive-mode-settings")
    public final ReactiveModeSettings reactiveMode = new ReactiveModeSettings();

    public static class ReactiveModeSettings extends ConfigSection {

        @Entry(key = "decrease-mspt-threshold")
        public final ConfigEntry<Double> decreaseMsptThreshold = ConfigEntries.doubleEntry();

        @Entry(key = "increase-mspt-threshold")
        public final ConfigEntry<Double> increaseMsptThreshold = ConfigEntries.doubleEntry();

        @Section(key = "reactive-view-distance")
        public final ReactiveViewDistanceSettings reactiveViewDistance = new ReactiveModeSettings.ReactiveViewDistanceSettings();

        @Section(key = "mspt-prediction")
        public final ReactiveModeSettings.MsptPredictionSettings msptPrediction = new ReactiveModeSettings.MsptPredictionSettings();

        @Section(key = "mspt-tracker-settings")
        public final ReactiveModeSettings.MsptTrackerSettings msptTracker = new ReactiveModeSettings.MsptTrackerSettings();

        public static class ReactiveViewDistanceSettings extends ConfigSection {

            @Entry(key = "use-reactive-view-distance")
            public final ConfigEntry<Boolean> useReactiveViewDistance = new ConfigEntry<>();

            @Entry(key = "target-view-distance-ratio")
            public final ConfigEntry<Double> targetViewDistanceRatio = ConfigEntries.doubleEntry();

        }

        public static class MsptTrackerSettings extends ConfigSection {

            @Entry(key = "collection-period")
            public final ConfigEntry<Integer> collectionPeriod = ConfigEntries.integerEntry();

        }

        public static class MsptPredictionSettings extends ConfigSection {

            @Entry(key = "enabled")
            public final ConfigEntry<Boolean> enabled = new ConfigEntry<>();

            @Entry(key = "history-length")
            public final ConfigEntry<Long> historyLength = ConfigEntries.longEntry();

        }

    }

    @Entry(key = "ticks-per-check")
    public final ConfigEntry<Long> ticksPerCheck = ConfigEntries.longEntry();

    @Entry(key = "start-up-delay")
    public final ConfigEntry<Long> startUpDelay = ConfigEntries.longEntry();

    @Entry(key = "passed-checks-for-increase")
    public final ConfigEntry<Integer> passedChecksForIncrease = new ConfigEntry<>();

    @Entry(key = "passed-checks-for-decrease")
    public final ConfigEntry<Integer> passedChecksForDecrease = new ConfigEntry<>();

    @Entry(key = "log-changes")
    public final ConfigEntry<Boolean> logChanges = new ConfigEntry<>();

    @SectionMap(key = "world-settings", defaultKey = "default")
    public final ConfigSectionMap<World, WorldSettings> worldSettings = new ConfigSectionMap<>(World::getName, WorldSettings.class, true);

    public static class WorldSettings extends ConfigSection {

        @Section(key = "simulation-distance")
        public final SimulationDistanceSettings simulationDistance = new SimulationDistanceSettings();

        @Section(key = "view-distance")
        public final ViewDistanceSettings viewDistance = new ViewDistanceSettings();

        @Section(key = "chunk-counter-settings")
        public final ChunkCounterSettings chunkCounter = new ChunkCounterSettings();

        @Entry(key = "chunk-weight")
        public final ConfigEntry<Double> chunkWeight = ConfigEntries.doubleEntry();

        public static class SimulationDistanceSettings extends ConfigSection {

            @Entry(key = "exclude")
            public final ConfigEntry<Boolean> exclude = new ConfigEntry<>();

            @Entry(key = "minimum-simulation-distance")
            public final ConfigEntry<Integer> minimumSimulationDistance = new ConfigEntry<>();

            @Entry(key = "maximum-simulation-distance")
            public final ConfigEntry<Integer> maximumSimulationDistance = new ConfigEntry<>();

        }

        public static class ViewDistanceSettings extends ConfigSection {

            @Entry(key = "exclude")
            public final ConfigEntry<Boolean> exclude = new ConfigEntry<>();

            @Entry(key = "minimum-view-distance")
            public final ConfigEntry<Integer> minimumViewDistance = new ConfigEntry<>();

            @Entry(key = "maximum-view-distance")
            public final ConfigEntry<Integer> maximumViewDistance = new ConfigEntry<>();

        }

        public static class ChunkCounterSettings extends ConfigSection {

            @Entry(key = "exclude-overlap")
            public final ConfigEntry<Boolean> excludeOverlap = new ConfigEntry<>();

        }

    }

}
