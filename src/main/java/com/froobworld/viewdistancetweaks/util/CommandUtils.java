package com.froobworld.viewdistancetweaks.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandUtils {

    public static boolean permissionCheck(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }
        return true;
    }

}
