package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.limiter.ManualViewDistanceManager;
import com.froobworld.viewdistancetweaks.util.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SetCommand implements CommandExecutor {
    private final Supplier<ManualViewDistanceManager> manualViewDistanceManager;
    private final String setMessage;
    private final String setMessageNoLimit;

    public SetCommand(Supplier<ManualViewDistanceManager> manualViewDistanceManager, String setMessage, String setMessageNoLimit) {
        this.manualViewDistanceManager = manualViewDistanceManager;
        this.setMessage = setMessage;
        this.setMessageNoLimit = setMessageNoLimit;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cl, String[] args) {
        String[] nonFlagArgs = CommandUtils.getNonFlagArgs(args);
        if (nonFlagArgs.length < 2) {
            sender.sendMessage(ChatColor.RED + "Too few arguments.");
            return false;
        }
        boolean badViewDistance = false;
        int viewDistance = 0;
        try {
            viewDistance = Integer.parseInt(nonFlagArgs[1]);
        } catch (NumberFormatException e) {
            badViewDistance = true;
        }
        if (badViewDistance || viewDistance < 2 || viewDistance > 32) {
            sender.sendMessage(ChatColor.RED + "View distance must be an integer in the range [2,32].");
            return true;
        }
        Predicate<World> worldPredicate;
        if (nonFlagArgs.length > 2) {
            worldPredicate = world -> world.getName().matches(Arrays.stream(nonFlagArgs, 2, nonFlagArgs.length).collect(Collectors.joining(" ")));
        } else {
            worldPredicate = w -> true;
        }

        String[] durationArgs = CommandUtils.getFlagArgs("duration", args);
        boolean badDuration = false;
        Integer durationMinutes = null;
        try {
            durationMinutes = durationArgs.length == 0 ? null : Integer.valueOf(durationArgs[0]);
        } catch (NumberFormatException e) {
            badDuration = true;
        }
        if (badDuration || durationMinutes != null && durationMinutes <= 0) {
            sender.sendMessage(ChatColor.RED + "The duration must be a non-negative integer.");
            return true;
        }

        Set<World> worlds = Bukkit.getWorlds().stream()
                .filter(worldPredicate)
                .collect(Collectors.toSet());
        if (worlds.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No worlds matched predicate.");
            return true;
        }
        long durationTicks = durationMinutes == null ? Integer.MAX_VALUE : (TimeUnit.MINUTES.toSeconds(durationMinutes) * 20);
        int finalViewDistance = viewDistance;

        worlds.forEach(world -> manualViewDistanceManager.get().setViewDistance(world, finalViewDistance, durationTicks));
        if (durationMinutes == null) {
            sender.sendMessage(MessageFormat.format(setMessageNoLimit, CommandUtils.collectionToString(worlds, World::getName), viewDistance));
        } else {
            sender.sendMessage(MessageFormat.format(setMessage, CommandUtils.collectionToString(worlds, World::getName), viewDistance, durationMinutes));
        }
        return true;
    }

}
