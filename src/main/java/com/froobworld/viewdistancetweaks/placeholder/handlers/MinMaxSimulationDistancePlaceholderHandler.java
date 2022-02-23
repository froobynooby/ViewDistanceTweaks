package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class MinMaxSimulationDistancePlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public MinMaxSimulationDistancePlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return params.startsWith("max_simulation_distance") || params.startsWith("min_simulation_distance");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        if (params.startsWith("max_simulation_distance")) {
            World world = null;

            if (params.equalsIgnoreCase("max_simulation_distance")) {
                world = player.getWorld();
            } else if (params.startsWith("max_simulation_distance_")) {
                world = Bukkit.getWorld(params.replace("max_simulation_distance_", ""));
            }
            return world == null ? null : ("" + viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.maximumSimulationDistance.get());
        } else {
            World world = null;

            if (params.equalsIgnoreCase("min_simulation_distance")) {
                world = player.getWorld();
            } else if (params.startsWith("min_simulation_distance_")) {
                world = Bukkit.getWorld(params.replace("min_simulation_distance_", ""));
            }
            return world == null ? null : ("" + viewDistanceTweaks.getVdtConfig().worldSettings.of(world).simulationDistance.minimumSimulationDistance.get());
        }
    }
}
