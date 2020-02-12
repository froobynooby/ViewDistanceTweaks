package com.froobworld.viewdistancetweaks.config;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Config {
    private ViewDistanceTweaks viewDistanceTweaks;
    private YamlConfiguration yamlConfiguration;

    public Config(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    public void load() throws IOException {
        File configFile = new File(viewDistanceTweaks.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            viewDistanceTweaks.getLogger().info("No config.yml found, copying from jar...");
            Files.copy(Objects.requireNonNull(viewDistanceTweaks.getResource("resources/config.yml")), configFile.toPath());
            viewDistanceTweaks.getLogger().info("Copied config.yml.");
        }
        viewDistanceTweaks.getLogger().info("Loading configuration...");
        yamlConfiguration = YamlConfiguration.loadConfiguration(configFile);
        viewDistanceTweaks.getLogger().info("Successfully loaded configuration.");
    }

    public boolean isEnabled() {
        return yamlConfiguration.getBoolean("enabled");
    }

    public int getTicksPerCheck() {
        return yamlConfiguration.getInt("ticks-per-check");
    }

    public int getTargetGlobalChunkCount() {
        return yamlConfiguration.getInt("global-chunk-count-target");
    }

    public double getChunkWeight(World world) {
        return yamlConfiguration.getDouble("world-settings." + world.getName() + ".chunk-weight",
                yamlConfiguration.getDouble("world-settings.default.chunk-weight"));
    }

    public int getMinimumViewDistance(World world) {
        return yamlConfiguration.getInt("world-settings." + world.getName() + ".minimum-view-distance",
                yamlConfiguration.getInt("world-settings.default.minimum-view-distance"));
    }

    public int getMaximumViewDistance(World world) {
        return yamlConfiguration.getInt("world-settings." + world.getName() + ".maximum-view-distance",
                yamlConfiguration.getInt("world-settings.default.maximum-view-distance"));
    }

    public long getSmoothChangePeriod() {
        return yamlConfiguration.getLong("smooth-change-period");
    }

    public int getPassedChecksForIncrease() {
        return yamlConfiguration.getInt("passed-checks-for-increase");
    }

    public int getPassedChecksForDecrease() {
        return yamlConfiguration.getInt("passed-checks-for-decrease");
    }

    public boolean logViewDistanceChangs() {
        return yamlConfiguration.getBoolean("log-view-distance-changes");
    }

}
