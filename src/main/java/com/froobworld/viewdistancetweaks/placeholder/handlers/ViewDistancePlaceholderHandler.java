package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ViewDistancePlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public ViewDistancePlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return viewDistanceTweaks.getHookManager().getViewDistanceHook() != null && params.startsWith("view_distance");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        World world = null;

        if (params.equalsIgnoreCase("view_distance")) {
            world = player.getWorld();
        } else if (params.startsWith("view_distance_")) {
            world = Bukkit.getWorld(params.replace("view_distance_", ""));
        }
        return world == null ? null : ("" + viewDistanceTweaks.getHookManager().getViewDistanceHook().getDistance(world));
    }
}
