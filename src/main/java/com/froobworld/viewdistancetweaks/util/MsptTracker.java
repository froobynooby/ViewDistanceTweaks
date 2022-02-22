package com.froobworld.viewdistancetweaks.util;

import com.froobworld.viewdistancetweaks.hook.tick.TickHook;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MsptTracker {
    private static final long MILLIS_PER_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private final Consumer<Long> tickConsumer = this::onTickEnd;
    private final TickHook tickHook;
    private final int collectionPeriod;
    private final Queue<Long> tickDurations;
    private Double cachedMspt;

    public MsptTracker(int collectionPeriod, TickHook tickHook) {
        this.tickHook = tickHook;
        this.collectionPeriod = collectionPeriod <= 0 ? 1200 : collectionPeriod;
        tickDurations = new ArrayDeque<>();
    }

    public void register() {
        tickHook.addTickConsumer(tickConsumer);
    }

    public void unregister() {
        tickHook.removeTickConsumer(tickConsumer);
        tickDurations.clear();
    }

    public double getMspt() {
        if (cachedMspt == null) {
            List<Long> sortedTickDurations = new ArrayList<>(tickDurations);
            sortedTickDurations.sort(null);
            if (sortedTickDurations.isEmpty()) {
                cachedMspt = 25.0;
            } else if (sortedTickDurations.size() % 2 == 0) {
                cachedMspt = 0.5 * (sortedTickDurations.get(sortedTickDurations.size() / 2) + sortedTickDurations.get(sortedTickDurations.size() / 2 - 1)) / MILLIS_PER_NANOS;
            } else {
                cachedMspt = (double) sortedTickDurations.get(sortedTickDurations.size() / 2) / MILLIS_PER_NANOS;
            }
        }
        return cachedMspt;
    }

    private void onTickEnd(long tickDuration) {
        if (tickDurations.size() >= collectionPeriod) {
            tickDurations.remove();
        }
        tickDurations.add(tickDuration);
        cachedMspt = null;
    }

}
