package com.froobworld.viewdistancetweaks.placeholder;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.placeholder.handlers.*;
import com.google.common.collect.Lists;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VdtExpansion extends PlaceholderExpansion {
    private static final String IDENTIFIER = "viewdistancetweaks";
    private static final String AUTHOR = "froobynooby";
    private static final String VERSION = "1";

    private final List<PlaceholderHandler> placeholderHandlers;

    public VdtExpansion(ViewDistanceTweaks viewDistanceTweaks) {
        placeholderHandlers = Lists.newArrayList(
                new SimulationDistancePlaceholderHandler(viewDistanceTweaks),
                new ViewDistancePlaceholderHandler(viewDistanceTweaks),
                new MinMaxSimulationDistancePlaceholderHandler(viewDistanceTweaks),
                new MinMaxViewDistancePlaceholderHandler(viewDistanceTweaks),
                new ChunkCountPlaceholderHandler(viewDistanceTweaks),
                new GlobalChunkCountPlaceholderHandler(viewDistanceTweaks),
                new MsptPlaceholderHandler(viewDistanceTweaks),
                new TpsPlaceholderHandler(viewDistanceTweaks)
        );
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
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        for (PlaceholderHandler handler : placeholderHandlers) {
            if (handler.shouldHandle(params)) {
                return handler.handlePlaceholder(player, params);
            }
        }
        return null;
    }

}
