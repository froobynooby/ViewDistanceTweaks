package com.froobworld.viewdistancetweaks.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandUtils {

    public static boolean permissionCheck(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }
        return true;
    }

    public static <T> String collectionToString(Collection<? extends T> collection, Function<T, String> stringFunction) {
        return collection.stream()
                .map(stringFunction)
                .collect(Collectors.joining(", "));
    }

    public static String collectionToString(Collection<?> collection) {
        return collectionToString(collection, Object::toString);
    }

    public static String[] getFlagArgs(String flagName, String[] args) {
        boolean reachedFlag = false;
        List<String> flagArgs = new ArrayList<>();
        for (String arg : args) {
            if (reachedFlag && arg.startsWith("--")) {
                break;
            } else if (reachedFlag) {
                flagArgs.add(arg);
            }
            if (arg.equalsIgnoreCase("--" + flagName)) {
                reachedFlag = true;
            }
        }
        return flagArgs.toArray(new String[0]);
    }

    public static String[] getNonFlagArgs(String[] args) {
        List<String> nonFlagArgs = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                break;
            }
            nonFlagArgs.add(arg);
        }
        return nonFlagArgs.toArray(new String[0]);
    }

}
