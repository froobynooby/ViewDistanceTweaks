package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.command.VdtCommand;
import com.froobworld.viewdistancetweaks.config.VdtConfig;
import com.froobworld.viewdistancetweaks.metrics.VdtMetrics;
import com.froobworld.viewdistancetweaks.placeholder.VdtExpansion;
import com.froobworld.viewdistancetweaks.util.*;
import com.froobworld.viewdistancetweaks.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ViewDistanceTweaks extends JavaPlugin {
    private VdtConfig vdtConfig;
    private HookManager hookManager;
    private TaskManager taskManager;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception ex) {
            getLogger().severe("ViewDistanceTweaks requires Spigot (or a fork such as Paper) in order to run.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        vdtConfig = new VdtConfig(this);
        try {
            vdtConfig.load();
        } catch (Exception e) {
            getLogger().severe("Exception while loading configuration:");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!vdtConfig.enabled.get()) {
            getLogger().warning("ViewDistanceTweaks must be configured before it can be enabled. Edit the " +
                    "config.yml file in the plugin's data folder, setting the 'enabled' option to true when you are " +
                    "done, then reload or restart the server.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ViewDistanceUtils.syncSpigotViewDistances();

        hookManager = new HookManager(this);
        hookManager.init();
        taskManager = new TaskManager(this);
        taskManager.init();
        registerCommands();
        initMetrics();

        if (Bukkit.getPluginManager().getPlugin("PlaceHolderAPI") != null) {
            if (new VdtExpansion(this).register()) {
                getLogger().info("Registered expansion for PlaceHolderAPI.");
            }
        }

        getLogger().info("Finished startup.");
    }

    @Override
    public void onDisable() {}

    public void reload() throws Exception {
        vdtConfig.load();
        taskManager.reload();
    }

    private void registerCommands() {
        VdtCommand vdtCommand = new VdtCommand(this);
        getCommand("vdt").setExecutor(vdtCommand);
        getCommand("vdt").setPermission(VdtCommand.PERMISSION);
        getCommand("vdt").setTabCompleter(vdtCommand.getTabCompleter());
    }

    private void initMetrics() {
        VdtMetrics.addCustomMetrics(new Metrics(this, 6488), this);
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public VdtConfig getVdtConfig() {
        return vdtConfig;
    }

}
