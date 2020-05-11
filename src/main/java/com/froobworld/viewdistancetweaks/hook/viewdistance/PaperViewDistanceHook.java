package com.froobworld.viewdistancetweaks.hook.viewdistance;

import com.froobworld.viewdistancetweaks.util.ViewDistanceUtils;
import org.bukkit.World;

public class PaperViewDistanceHook extends SpigotViewDistanceHook {

    @Override
    public void setViewDistance(World world, int value) {
        value = ViewDistanceUtils.clampViewDistance(value);
        if (value != getViewDistance(world)) {
            world.setViewDistance(value);
        }
    }

    public static boolean isCompatible() {
        try {
            Class.forName("org.bukkit.World")
                    .getDeclaredMethod("setViewDistance", int.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
