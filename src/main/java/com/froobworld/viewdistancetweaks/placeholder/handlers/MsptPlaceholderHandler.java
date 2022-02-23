package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MsptPlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public MsptPlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return (params.equalsIgnoreCase("mspt_colour") || params.equalsIgnoreCase("mspt_color")) || params.equalsIgnoreCase("mspt");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        if ((params.equalsIgnoreCase("mspt_colour") || params.equalsIgnoreCase("mspt_color"))) {
            double mspt = viewDistanceTweaks.getTaskManager().getMsptTracker().getMspt();
            if (mspt > 50) {
                return ChatColor.RED + "";
            } else if (mspt > 40) {
                return ChatColor.YELLOW + "";
            } else {
                return ChatColor.GREEN + "";
            }
        } else {
            return BigDecimal.valueOf(viewDistanceTweaks.getTaskManager().getMsptTracker().getMspt()).setScale(2, RoundingMode.HALF_UP).toString();
        }
    }
}
