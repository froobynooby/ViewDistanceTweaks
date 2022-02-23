package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GlobalChunkCountPlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public GlobalChunkCountPlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return params.equalsIgnoreCase("global_tick_chunk_count") ||
                (viewDistanceTweaks.getHookManager().getViewDistanceHook() != null && params.equalsIgnoreCase("global_no_tick_chunk_count")) ||
                params.equalsIgnoreCase("global_chunk_count");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        SimulationDistanceHook simulationDistanceHook = viewDistanceTweaks.getHookManager().getSimulationDistanceHook();
        ViewDistanceHook viewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        ChunkCounter chunkCounter = viewDistanceTweaks.getHookManager().getChunkCounter();
        ChunkCounter noTickChunkCounter = viewDistanceTweaks.getHookManager().getNoTickChunkCounter();
        if (params.startsWith("global_tick_chunk_count")) {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                count += chunkCounter.countChunks(world, simulationDistanceHook.getDistance(world));
            }
            return "" + count;
        } else if (viewDistanceHook != null && params.startsWith("no_tick_chunk_count")) {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                count += noTickChunkCounter.countChunks(world, viewDistanceHook.getDistance(world));
            }
            return "" + count;
        } else {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                count += chunkCounter.countChunks(world, simulationDistanceHook.getDistance(world));
                if (viewDistanceHook != null) {
                    count += noTickChunkCounter.countChunks(world, viewDistanceHook.getDistance(world));
                }
            }
            return "" + count;
        }
    }
}
