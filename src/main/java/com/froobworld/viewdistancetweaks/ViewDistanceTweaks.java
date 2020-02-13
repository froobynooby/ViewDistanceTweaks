package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.command.VdtCommand;
import com.froobworld.viewdistancetweaks.config.Config;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceLimiter;
import com.froobworld.viewdistancetweaks.metrics.Metrics;
import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class ViewDistanceTweaks extends JavaPlugin {
    private Config config;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception ex) {
            getLogger().severe("ViewDistanceTweaks requires Spigot (or a fork such as Paper) in order to run.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        config = new Config(this);
        try {
            config.load();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!config.isEnabled()) {
            getLogger().warning("ViewDistanceTweaks must be configured before it can be enabled. Edit the " +
                    "config.yml file in the plugin's data folder, setting the 'enabled' option to true when you are " +
                    "done, then reload or restart the server.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ViewDistanceUtils.syncSpigotViewDistances();
        new ViewDistanceLimiter(this);

        getCommand("vdt").setExecutor(new VdtCommand(this));
        getCommand("vdt").setPermission(VdtCommand.PERMISSON);
        getCommand("vdt").setTabCompleter(VdtCommand.tabCompleter);

        new Metrics(this, 6488);
    }

    @Override
    public void onDisable() {}

    public Config getViewDistanceTweaksConfig() {
        return config;
    }

}
