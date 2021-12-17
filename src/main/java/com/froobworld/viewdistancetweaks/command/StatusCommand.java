package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
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
        boolean weightedCounts = args.length > 1 && args[1].equalsIgnoreCase("--weight");
        SimulationDistanceHook simulationDistanceHook = viewDistanceTweaks.getHookManager().getSimulationDistanceHook();
        SimulationDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        ChunkCounter chunkCounter = weightedCounts ? viewDistanceTweaks.getHookManager().getChunkCounter() : viewDistanceTweaks.getHookManager().getActualChunkCounter();
        ChunkCounter noTickChunkCounter = weightedCounts ? viewDistanceTweaks.getHookManager().getNoTickChunkCounter() : viewDistanceTweaks.getHookManager().getActualNoTickChunkCounter();

        String statusMessage = ChatColor.RED + "{0} " + ChatColor.GRAY + "/" + ChatColor.RED + " {1}";
        String noTickStatusMessage = ChatColor.RED + "{0} " + ChatColor.GRAY + "/" + ChatColor.RED + " {1} " + ChatColor.GRAY + "/" + ChatColor.RED + " {2} " + ChatColor.GRAY + "/" + ChatColor.RED + " {3}";
        String format = noTickViewDistanceHook == null ? statusMessage : noTickStatusMessage;
        String globalStatusMessage = ChatColor.GRAY + "Total chunks: " + ChatColor.RED + "{0}";
        String noTickGlobalStatusMessage = ChatColor.GRAY + "Total chunks: " + ChatColor.RED + "{0}" + ChatColor.GRAY + " (" + ChatColor.RED + "{1}" + ChatColor.GRAY + " ticking, " + ChatColor.RED + "{2}" + ChatColor.GRAY + " non-ticking)";
        String globalStatusFormat = noTickViewDistanceHook == null ? globalStatusMessage : noTickGlobalStatusMessage;

        int totalChunks = 0;
        int totalNoTickChunks = 0;
        sender.sendMessage(ChatColor.GRAY + "Note: " + ChatColor.GREEN + "The chunk counts below are only heuristic.");
        sender.sendMessage(ChatColor.GRAY + "Format: " + MessageFormat.format(format, "sim d.", "chunks", "view d.", "no-tick chunks"));
        for (World world : Bukkit.getWorlds()) {
            int simulationDistance = simulationDistanceHook.getDistance(world);
            int loadedChunks = (int) chunkCounter.countChunks(world, simulationDistance);
            int viewDistance = 0;
            int loadedNoTickChunks = 0;
            totalChunks += loadedChunks;
            if (noTickViewDistanceHook != null) {
                viewDistance = noTickViewDistanceHook.getDistance(world);
                loadedNoTickChunks = (int) noTickChunkCounter.countChunks(world, viewDistance);
                totalNoTickChunks += loadedNoTickChunks;
            }

            sender.sendMessage(ChatColor.GOLD + world.getName());
            sender.sendMessage(MessageFormat.format(format, Integer.toString(simulationDistance), Integer.toString(loadedChunks), Integer.toString(viewDistance), Integer.toString(loadedNoTickChunks)));
        }
        sender.sendMessage(ChatColor.GRAY + "--------");
        sender.sendMessage(MessageFormat.format(globalStatusFormat, totalChunks + totalNoTickChunks, totalChunks, totalNoTickChunks));

        return true;
    }

}
