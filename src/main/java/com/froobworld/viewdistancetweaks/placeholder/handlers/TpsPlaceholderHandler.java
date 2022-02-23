package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TpsPlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public TpsPlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return (params.equalsIgnoreCase("tps_colour") || params.equalsIgnoreCase("tps_color")) || params.equalsIgnoreCase("tps");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        if ((params.equalsIgnoreCase("tps_colour") || params.equalsIgnoreCase("tps_color"))) {
            double tps = viewDistanceTweaks.getTaskManager().getTpsTracker().getTps();
            if (tps < 16) {
                return ChatColor.RED + "";
            } else if (tps < 18) {
                return ChatColor.YELLOW + "";
            } else {
                return ChatColor.GREEN + "";
            }
        } else {
            return BigDecimal.valueOf(Math.min(viewDistanceTweaks.getTaskManager().getTpsTracker().getTps(), 20)).setScale(2, RoundingMode.HALF_UP).toString();
        }
    }
}
