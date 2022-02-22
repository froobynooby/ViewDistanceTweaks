package com.froobworld.viewdistancetweaks.hook.viewdistance;

import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class PaperViewDistanceHook implements ViewDistanceHook {

    @Override
    public int getDistance(World world) {
        return world.getViewDistance();
    }

    @Override
    public void setDistance(World world, int value) {
        value = ViewDistanceUtils.clampViewDistance(value);
        if (value != getDistance(world)) {
            world.setViewDistance(value);
        }
    }

    public static boolean isCompatible() {
        try {
            Class.forName("org.bukkit.World")
                    .getMethod("setSimulationDistance", int.class);
        } catch (Exception exception) {
            return false;
        }
        try {
            for (World world : Bukkit.getWorlds()) {
                world.setViewDistance(0);
            }
        } catch (IllegalArgumentException e) {
            return true;
        } catch (Exception otherException) {
            return false;
        }
        return true;
    }

}
