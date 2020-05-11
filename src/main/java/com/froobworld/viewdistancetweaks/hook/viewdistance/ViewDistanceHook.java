package com.froobworld.viewdistancetweaks.hook.viewdistance;

import org.bukkit.World;

public interface ViewDistanceHook {

    int getViewDistance(World world);

    void setViewDistance(World world, int value);

}
