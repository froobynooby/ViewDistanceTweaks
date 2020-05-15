package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
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
        ViewDistanceHook viewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        ViewDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook();
        ChunkCounter chunkCounter = viewDistanceTweaks.getHookManager().getChunkCounter();
        ChunkCounter noTickChunkCounter = viewDistanceTweaks.getHookManager().getNoTickChunkCounter();

        String statusMessage = ChatColor.RED + "{0} " + ChatColor.GRAY + "/" + ChatColor.RED + " {1}";
        String noTickStatusMessage = ChatColor.RED + "{0} " + ChatColor.GRAY + "/" + ChatColor.RED + " {1} " + ChatColor.GRAY + "/" + ChatColor.RED + " {2} " + ChatColor.GRAY + "/" + ChatColor.RED + " {3}";
        String format = noTickViewDistanceHook == null ? statusMessage : noTickStatusMessage;
        String globalStatusMessage = ChatColor.GRAY + "Total chunks: " + ChatColor.RED + "{0}";
        String noTickGlobalStatusMessage = ChatColor.GRAY + "Total chunks: " + ChatColor.RED + "{0}" + ChatColor.GRAY + " (" + ChatColor.RED + "{1}" + ChatColor.GRAY + " ticking, " + ChatColor.RED + "{2}" + ChatColor.GRAY + " non-ticking)";
        String globalStatusFormat = noTickViewDistanceHook == null ? globalStatusMessage : noTickGlobalStatusMessage;

        int totalChunks = 0;
        int totalNoTickChunks = 0;
        sender.sendMessage(ChatColor.GRAY + "Note: " + ChatColor.GREEN + "The chunk counts below are only heuristic.");
        sender.sendMessage(ChatColor.GRAY + "Format: " + MessageFormat.format(format, "view d.", "chunks", "no-tick view d.", "no-tick chunks"));
        for (World world : Bukkit.getWorlds()) {
            int viewDistance = viewDistanceHook.getViewDistance(world);
            int loadedChunks = (int) chunkCounter.countChunks(world, viewDistance);
            int noTickViewDistance = 0;
            int loadedNoTickChunks = 0;
            totalChunks += loadedChunks;
            if (noTickViewDistanceHook != null) {
                noTickViewDistance = noTickViewDistanceHook.getViewDistance(world);
                loadedNoTickChunks = (int) noTickChunkCounter.countChunks(world, noTickViewDistance);
                totalNoTickChunks += loadedNoTickChunks;
            }

            sender.sendMessage(ChatColor.GOLD + world.getName());
            sender.sendMessage(MessageFormat.format(format, Integer.toString(viewDistance), Integer.toString(loadedChunks), Integer.toString(noTickViewDistance), Integer.toString(loadedNoTickChunks)));
        }
        sender.sendMessage(ChatColor.GRAY + "--------");
        sender.sendMessage(MessageFormat.format(globalStatusFormat, totalChunks + totalNoTickChunks, totalChunks, totalNoTickChunks));

        return true;
    }

}
