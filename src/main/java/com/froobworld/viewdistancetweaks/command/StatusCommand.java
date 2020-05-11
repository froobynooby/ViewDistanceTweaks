package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;

public class StatusCommand implements CommandExecutor {
    private ViewDistanceTweaks viewDistanceTweaks;

    public StatusCommand(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String statusMessage = ChatColor.RED + "{0} " + ChatColor.GRAY + "/" + ChatColor.RED + " {1}";
        String noTickStatusMessage = ChatColor.RED + "{0} " + ChatColor.GRAY + "/" + ChatColor.RED + " {1} " + ChatColor.GRAY + "/" + ChatColor.RED + " {2} " + ChatColor.GRAY + "/" + ChatColor.RED + " {3}";
        String format = viewDistanceTweaks.getNoTickViewDistanceHook() == null ? statusMessage : noTickStatusMessage;

        int totalChunks = 0;
        int totalNoTickChunks = 0;
        sender.sendMessage(ChatColor.GRAY + "Format: " + MessageFormat.format(format, "view d.", "chunks", "no-tick view d.", "no-tick chunks"));
        for (World world : Bukkit.getWorlds()) {
            int viewDistance = viewDistanceTweaks.getViewDistanceHook().getViewDistance(world);
            int loadedChunks = (int) viewDistanceTweaks.getChunkCounter().countChunks(world, viewDistance);
            int noTickViewDistance = 0;
            int loadedNoTickChunks = 0;
            totalChunks += loadedChunks;
            if (viewDistanceTweaks.getNoTickViewDistanceHook() != null) {
                noTickViewDistance = viewDistanceTweaks.getNoTickViewDistanceHook().getViewDistance(world);
                loadedNoTickChunks = (int) viewDistanceTweaks.getNoTickChunkCounter().countChunks(world, noTickViewDistance);
                totalNoTickChunks += loadedNoTickChunks;
            }

            sender.sendMessage(ChatColor.GOLD + world.getName());
            sender.sendMessage(MessageFormat.format(format, Integer.toString(viewDistance), Integer.toString(loadedChunks), Integer.toString(noTickViewDistance), Integer.toString(loadedNoTickChunks)));
        }
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "Global");
        sender.sendMessage(ChatColor.GRAY + "Loaded chunks: " + ChatColor.RED + (totalChunks + totalNoTickChunks)
                + ChatColor.GRAY + " (" + ChatColor.RED + totalChunks + ChatColor.GRAY + " ticking, " + ChatColor.RED + totalNoTickChunks + ChatColor.GRAY + " non-ticking" + ")");

        return true;
    }

}
