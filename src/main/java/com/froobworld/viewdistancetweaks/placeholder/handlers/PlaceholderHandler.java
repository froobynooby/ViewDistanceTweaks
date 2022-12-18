package com.froobworld.viewdistancetweaks.placeholder.handlers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class PlaceholderHandler {

    public abstract boolean shouldHandle(String params);

    public abstract String handlePlaceholder(Player player, String params);


    protected static World getWorld(Player player) {
        if (player == null) {
            return Bukkit.getWorlds().get(0);
        }
        return player.getWorld();
    }
}
