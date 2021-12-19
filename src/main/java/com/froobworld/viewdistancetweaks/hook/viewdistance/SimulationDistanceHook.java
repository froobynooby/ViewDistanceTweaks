package com.froobworld.viewdistancetweaks.hook.viewdistance;

import org.bukkit.World;

public interface SimulationDistanceHook {

    int getDistance(World world);

    void setDistance(World world, int value);

}
