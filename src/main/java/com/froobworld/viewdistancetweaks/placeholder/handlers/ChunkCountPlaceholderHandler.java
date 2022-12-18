package com.froobworld.viewdistancetweaks.placeholder.handlers;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SimulationDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChunkCountPlaceholderHandler extends PlaceholderHandler {
    private final ViewDistanceTweaks viewDistanceTweaks;

    public ChunkCountPlaceholderHandler(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public boolean shouldHandle(String params) {
        return params.startsWith("tick_chunk_count") ||
                (viewDistanceTweaks.getHookManager().getViewDistanceHook() != null && params.startsWith("no_tick_chunk_count")) ||
                params.startsWith("chunk_count");
    }

    @Override
    public String handlePlaceholder(Player player, String params) {
        SimulationDistanceHook simulationDistanceHook = viewDistanceTweaks.getHookManager().getSimulationDistanceHook();
        ViewDistanceHook viewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        ChunkCounter chunkCounter = viewDistanceTweaks.getHookManager().getChunkCounter();
        ChunkCounter noTickChunkCounter = viewDistanceTweaks.getHookManager().getNoTickChunkCounter();
        if (params.startsWith("tick_chunk_count")) {
            World world = null;

            if (params.equalsIgnoreCase("tick_chunk_count")) {
                world = getWorld(player);
            } else if (params.startsWith("tick_chunk_count_")) {
                world = Bukkit.getWorld(params.replace("tick_chunk_count_", ""));
            }
            return world == null ? null : ("" + (int) chunkCounter.countChunks(world, simulationDistanceHook.getDistance(world)));
        } else if (viewDistanceHook != null && params.startsWith("no_tick_chunk_count")) {
            World world = null;

            if (params.equalsIgnoreCase("no_tick_chunk_count")) {
                world = getWorld(player);
            } else if (params.startsWith("no_tick_chunk_count_")) {
                world = Bukkit.getWorld(params.replace("no_tick_chunk_count_", ""));
            }
            return world == null ? null : ("" + (int) noTickChunkCounter.countChunks(world, viewDistanceHook.getDistance(world)));
        } else {
            World world = null;

            if (params.equalsIgnoreCase("chunk_count")) {
                world = getWorld(player);
            } else if (params.startsWith("chunk_count_")) {
                world = Bukkit.getWorld(params.replace("chunk_count_", ""));
            }
            return world == null ? null : ("" + (int) chunkCounter.countChunks(world, viewDistanceHook == null ? simulationDistanceHook.getDistance(world) : viewDistanceHook.getDistance(world)));
        }
    }
}
