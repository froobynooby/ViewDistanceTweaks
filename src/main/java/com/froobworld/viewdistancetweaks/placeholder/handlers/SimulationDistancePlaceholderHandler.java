package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SimulationDistancePlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public SimulationDistancePlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return params.startsWith("simulation_distance");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        World world = null;

        if (params.equalsIgnoreCase("simulation_distance")) {
            world = player.getWorld();
        } else if (params.startsWith("simulation_distance_")) {
            world = Bukkit.getWorld(params.replace("simulation_distance_", ""));
        }
        return world == null ? null : ("" + viewDistanceTweaks.getHookManager().getSimulationDistanceHook().getDistance(world));
    }
}
