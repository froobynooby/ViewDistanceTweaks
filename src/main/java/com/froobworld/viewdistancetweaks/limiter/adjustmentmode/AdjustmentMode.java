package com.froobworld.viewdistancetweaks.limiter.adjustmentmode;

import org.bukkit.World;

import java.util.Collection;
import java.util.Map;

public interface AdjustmentMode {

    Map<World, Adjustment> getAdjustments(Collection<World> worlds, boolean mutate);

    default Map<World, Adjustment> getAdjustments(Collection<World> worlds) {
        return getAdjustments(worlds, true);
    }

    enum Adjustment {
        INCREASE,
        DECREASE,
        STAY;

        public static Adjustment strongest(Adjustment adjustment1, Adjustment adjustment2) {
            if (adjustment1 == DECREASE || adjustment2 == DECREASE) {
                return DECREASE;
            } else if (adjustment1 == STAY || adjustment2 == STAY) {
                return STAY;
            } else {
                return INCREASE;
            }
        }
    }

    enum Mode {
        PROACTIVE,
        REACTIVE,
        MIXED;

        public static Mode fromString(String string) {
            if (string.equalsIgnoreCase("proactive")) {
                return PROACTIVE;
            } else if (string.equalsIgnoreCase("reactive")) {
                return REACTIVE;
            }
            return null;
        }

    }

}
