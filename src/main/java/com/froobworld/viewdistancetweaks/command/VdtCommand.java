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
import java.util.List;

public class VdtCommand implements CommandExecutor {
    public static final String PERMISSON = "viewdistancetweaks.command.vdt";
    public static final TabCompleter tabCompleter = new TabCompleter() {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 0) {
                return new ArrayList<>();
            }
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "status", "rl", "stats"), new ArrayList<>());
        }
    };

    private ViewDistanceTweaks viewDistanceTweaks;
    private ReloadCommand reloadCommand;
    private StatusCommand statusCommand;

    public VdtCommand(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
        reloadCommand = new ReloadCommand(viewDistanceTweaks);
        statusCommand = new StatusCommand(viewDistanceTweaks);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String cl, String[] args) {
        if (!CommandUtils.permissionCheck(sender, PERMISSON)) return true;

        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                return reloadCommand.onCommand(sender, command, cl, args);
            }
            if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("stats")) {
                return statusCommand.onCommand(sender, command, cl, args);
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "ViewDistanceTweeks v" + viewDistanceTweaks.getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage("/" + cl + " reload");
        sender.sendMessage("/" + cl + " status");
        return true;
    }




}
