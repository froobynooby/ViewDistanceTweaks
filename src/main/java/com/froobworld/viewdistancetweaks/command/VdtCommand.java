package com.froobworld.viewdistancetweaks.command;

import com.froobworld.viewdistancetweaks.ViewDistanceTweaks;
import com.froobworld.viewdistancetweaks.util.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VdtCommand implements CommandExecutor {
    public static final String PERMISSON = "viewdistancetweaks.command.vdt";
    private final TabCompleter tabCompleter = new TabCompleter() {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 0) {
                return new ArrayList<>();
            }
            if (args.length == 1) {
                List<String> possibleCompletions = new ArrayList<>(Arrays.asList("reload", "status", "rl", "stats", "set"));
                if (viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook() != null) {
                    possibleCompletions.add("set-no-tick");
                }
                return StringUtil.copyPartialMatches(args[0], possibleCompletions, new ArrayList<>());
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("set-no-tick"))) {
                return StringUtil.copyPartialMatches(args[1], IntStream.rangeClosed(2, 32).mapToObj(Integer::toString).collect(Collectors.toSet()), new ArrayList<>());
            }
            if (args.length > 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("set-no-tick"))) {
                if (Arrays.stream(args).noneMatch(s -> s.equalsIgnoreCase("--duration"))) {
                    return StringUtil.copyPartialMatches(args[args.length - 1], Collections.singletonList("--duration"), new ArrayList<>());
                } else {
                    return StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList(10 + "", 30 + "", 60 + "", 120 + ""), new ArrayList<>());
                }
            }
            return new ArrayList<>();
        }
    };

    private ViewDistanceTweaks viewDistanceTweaks;
    private ReloadCommand reloadCommand;
    private StatusCommand statusCommand;
    private SetCommand setCommand;
    private SetCommand setNoTickCommand;

    public VdtCommand(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        reloadCommand = new ReloadCommand(viewDistanceTweaks);
        statusCommand = new StatusCommand(viewDistanceTweaks);
        setCommand = new SetCommand(
                viewDistanceTweaks.getTaskManager()::getManualViewDistanceManager,
                ChatColor.GRAY + "Set view distance of " + ChatColor.RED +  "{0}" + ChatColor.GRAY +
                        " to " + ChatColor.RED + "{1}" + ChatColor.GRAY + " for " + ChatColor.RED + "{2}" + ChatColor.GRAY + " minutes.",
                ChatColor.GRAY + "Set view distance of " + ChatColor.RED +  "{0}" + ChatColor.GRAY +
                        " to " + ChatColor.RED + "{1}" + ChatColor.GRAY + " until next reload."
        );
        setNoTickCommand = new SetCommand(
                viewDistanceTweaks.getTaskManager()::getManualNoTickViewDistanceManager,
                ChatColor.GRAY + "Set no-tick view distance of " + ChatColor.RED +  "{0}" + ChatColor.GRAY +
                        " to " + ChatColor.RED + "{1}" + ChatColor.GRAY + " for " + ChatColor.RED + "{2}" + ChatColor.GRAY + " minutes.",
                ChatColor.GRAY + "Set no-tick view distance of " + ChatColor.RED +  "{0}" + ChatColor.GRAY +
                        " to " + ChatColor.RED + "{1}" + ChatColor.GRAY + " until next reload."
        );
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String cl, String[] args) {
        if (!CommandUtils.permissionCheck(sender, PERMISSON)) return true;

        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                if (CommandUtils.permissionCheck(sender, "viewdistancetweaks.vdt.command.reload")) {
                    return reloadCommand.onCommand(sender, command, cl, args);
                } else {
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("stats")) {
                if (CommandUtils.permissionCheck(sender, "viewdistancetweaks.vdt.command.status")) {
                    return statusCommand.onCommand(sender, command, cl, args);
                } else {
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (CommandUtils.permissionCheck(sender, "viewdistancetweaks.vdt.command.set")) {
                    return setCommand.onCommand(sender, command, cl, args);
                } else {
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("set-no-tick") && viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook() != null) {
                if (CommandUtils.permissionCheck(sender, "viewdistancetweaks.vdt.command.set-no-tick")) {
                    return setNoTickCommand.onCommand(sender, command, cl, args);
                } else {
                    return true;
                }
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "View Distance Tweaks v" + viewDistanceTweaks.getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage("/" + cl + " reload");
        sender.sendMessage("/" + cl + " status");
        sender.sendMessage("/" + cl + " set <view distance> [world] [--duration <minutes>]");
        if (viewDistanceTweaks.getHookManager().getNoTickViewDistanceHook() != null) {
            sender.sendMessage("/" + cl + " set-no-tick <view distance> [world] [--duration <minutes>]");
        }
        return true;
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }


}
