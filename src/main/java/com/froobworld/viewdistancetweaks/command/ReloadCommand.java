package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private ViewDistanceTweaks viewDistanceTweaks;

    public ReloadCommand(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            viewDistanceTweaks.reload();
            sender.sendMessage(ChatColor.YELLOW + "Plugin reloaded.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Something went wrong reloading the plugin, see the console for more.");
            e.printStackTrace();
        }
        return true;
    }

}
