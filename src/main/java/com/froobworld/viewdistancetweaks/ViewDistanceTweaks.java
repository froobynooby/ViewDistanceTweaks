package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.config.Config;
import com.froobworld.viewdistancetweaks.limiter.ViewDistanceLimiter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class ViewDistanceTweaks extends JavaPlugin {
    private Config config;

    @Override
    public void onEnable() {
        try {
            config = new Config(this);
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
        new ViewDistanceLimiter(this);
    }

    @Override
    public void onDisable() {}

    public Config getViewDistanceTweaksConfig() {
        return config;
    }

}
