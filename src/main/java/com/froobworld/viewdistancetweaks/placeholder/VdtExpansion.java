package com.froobworld.viewdistancetweaks.placeholder;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.notick.NoTickViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VdtExpansion extends PlaceholderExpansion {
    private static final String IDENTIFIER = "viewdistancetweaks";
    private static final String AUTHOR = "froobynooby";
    private static final String VERSION = "1";

    private final ViewDistanceTweaks viewDistanceTweaks;

    public VdtExpansion(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return AUTHOR;
    }

    @Override
    public @NotNull String getVersion() {
        return VERSION;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        ViewDistanceHook viewDistanceHook = viewDistanceTweaks.getHookManager().getViewDistanceHook();
        ChunkCounter chunkCounter = viewDistanceTweaks.getHookManager().getChunkCounter();
        NoTickViewDistanceHook noTickViewDistanceHook = viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook();
        ChunkCounter noTickChunkCounter = viewDistanceTweaks.getHookManager().getNoTickChunkCounter();
        if (params.startsWith("view_distance")) {
            World world = null;

            if (params.equalsIgnoreCase("view_distance")) {
                world = player.getWorld();
            } else if (params.startsWith("view_distance_")) {
                world = Bukkit.getWorld(params.replace("view_distance_", ""));
            }
            return world == null ? null : ("" + viewDistanceHook.getViewDistance(world));
        }

        if (noTickViewDistanceHook != null && params.startsWith("no_tick_view_distance")) {
            World world = null;

            if (params.equalsIgnoreCase("no_tick_view_distance")) {
                world = player.getWorld();
            } else if (params.startsWith("no_tick_view_distance_")) {
                world = Bukkit.getWorld(params.replace("no_tick_view_distance_", ""));
            }
            return world == null ? null : ("" + viewDistanceTweaks.getHookManager().getViewDistanceHook().getViewDistance(world));
        }

        if (params.startsWith("tick_chunk_count")) {
            World world = null;

            if (params.equalsIgnoreCase("tick_chunk_count")) {
                world = player.getWorld();
            } else if (params.startsWith("tick_chunk_count_")) {
                world = Bukkit.getWorld(params.replace("tick_chunk_count_", ""));
            }
            return world == null ? null : ("" + (int) chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world)));
        }

        if (noTickChunkCounter != null && noTickViewDistanceHook != null && params.startsWith("no_tick_chunk_count")) {
            World world = null;

            if (params.equalsIgnoreCase("no_tick_chunk_count")) {
                world = player.getWorld();
            } else if (params.startsWith("no_tick_chunk_count_")) {
                world = Bukkit.getWorld(params.replace("no_tick_chunk_count_", ""));
            }
            return world == null ? null : ("" + (int) noTickChunkCounter.countChunks(world, noTickViewDistanceHook.getViewDistance(world)));
        }

        if (params.startsWith("chunk_count")) {
            World world = null;

            if (params.equalsIgnoreCase("chunk_count")) {
                world = player.getWorld();
            } else if (params.startsWith("chunk_count_")) {
                world = Bukkit.getWorld(params.replace("chunk_count_", ""));
            }
            return world == null ? null : ("" + (noTickChunkCounter == null || noTickViewDistanceHook == null ? 0 : (int) noTickChunkCounter.countChunks(world, noTickViewDistanceHook.getViewDistance(world)) + (int) chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world))));
        }

        if (params.equalsIgnoreCase("global_tick_chunk_count")) {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                count += chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world));
            }
            return "" + count;
        }

        if (noTickChunkCounter != null && noTickViewDistanceHook != null && params.equalsIgnoreCase("global_no_tick_chunk_count")) {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                count += noTickChunkCounter.countChunks(world, noTickViewDistanceHook.getViewDistance(world));
            }
            return "" + count;
        }

        if (params.equalsIgnoreCase("global_chunk_count")) {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                count += chunkCounter.countChunks(world, viewDistanceHook.getViewDistance(world));
                if (noTickChunkCounter != null && noTickViewDistanceHook != null) {
                    count += noTickChunkCounter.countChunks(world, noTickViewDistanceHook.getViewDistance(world));
                }
            }
            return "" + count;
        }

        return null;
    }

}
