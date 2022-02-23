package com.froobworld.viewdistancetweaks.placeholder.handlers;

import org.bukkit.entity.Player;

public abstract class PlaceholderHandler {

    public abstract boolean shouldHandle(String params);

    public abstract String handlePlaceholder(Player player, String params);

}
