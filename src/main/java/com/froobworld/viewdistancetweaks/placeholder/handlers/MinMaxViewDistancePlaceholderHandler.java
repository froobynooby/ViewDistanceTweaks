package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class MinMaxViewDistancePlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public MinMaxViewDistancePlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return viewDistanceTweaks.getHookManager().getViewDistanceHook() != null && (params.startsWith("max_view_distance") || params.startsWith("min_view_distance"));
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        if (params.startsWith("max_view_distance")) {
            World world = null;

            if (params.equalsIgnoreCase("max_view_distance")) {
                world = player.getWorld();
            } else if (params.startsWith("max_view_distance_")) {
                world = Bukkit.getWorld(params.replace("max_view_distance_", ""));
            }
            return world == null ? null : ("" + viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.maximumViewDistance.get());
        } else {
            World world = null;

            if (params.equalsIgnoreCase("min_view_distance")) {
                world = player.getWorld();
            } else if (params.startsWith("min_view_distance_")) {
                world = Bukkit.getWorld(params.replace("min_view_distance_", ""));
            }
            return world == null ? null : ("" + viewDistanceTweaks.getVdtConfig().worldSettings.of(world).viewDistance.minimumViewDistance.get());
        }
    }
}
