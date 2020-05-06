package com.froobworld.viewdistancetweaks.util;

import org.bukkit.World;

public interface ViewDistanceHook {

    int getViewDistance(World world);

    void setViewDistance(World world, int value);

}
