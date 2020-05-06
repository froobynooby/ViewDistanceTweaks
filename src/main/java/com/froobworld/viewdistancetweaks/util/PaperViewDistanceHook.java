package com.froobworld.viewdistancetweaks.util;

import org.bukkit.World;

import static org.joor.Reflect.*;

public class PaperViewDistanceHook extends SpigotViewDistanceHook {

    @Override
    public void setViewDistance(World world, int value) {
        value = ViewDistanceUtils.clampViewDistance(value);
        if (value != getViewDistance(world)) {
            on(world).call("setViewDistance", value);
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
