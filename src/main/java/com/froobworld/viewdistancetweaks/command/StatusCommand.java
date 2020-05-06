package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StatusCommand implements CommandExecutor {
    private ViewDistanceTweaks viewDistanceTweaks;

    public StatusCommand(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int totalChunksLoaded = 0;
        int totalWeightedLoadedChunks = 0;
        sender.sendMessage(ChatColor.GRAY + "Format: view dist. / load. chunks / load. chunks (weighted)");
        for (World world : Bukkit.getWorlds()) {
            sender.sendMessage(ChatColor.YELLOW + world.getName());
            int viewDistance = viewDistanceTweaks.getViewDistanceHook().getViewDistance(world);
            int chunksLoaded = world.getLoadedChunks().length;
            int weightedChunksLoaded = (int) (chunksLoaded * viewDistanceTweaks.getViewDistanceTweaksConfig().getChunkWeight(world));
            totalChunksLoaded += chunksLoaded;
            totalWeightedLoadedChunks += weightedChunksLoaded;

            sender.sendMessage(ChatColor.RED + "" + viewDistance + ChatColor.GRAY
                    + " / " + ChatColor.RED + chunksLoaded + ChatColor.GRAY
                    + " / " + ChatColor.RED + weightedChunksLoaded);
        }
        sender.sendMessage(ChatColor.YELLOW + "Global");
        sender.sendMessage(ChatColor.GRAY + "Loaded chunks: " + ChatColor.RED + totalChunksLoaded);
        sender.sendMessage(ChatColor.GRAY + "Loaded chunks (weighted): " + ChatColor.RED + totalWeightedLoadedChunks);

        return false;
    }
}
