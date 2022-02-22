package com.froobworld.viewdistancetweaks.util;

import com.froobworld.viewdistancetweaks.hook.tick.TickHook;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.function.Consumer;

public class TpsTracker implements Consumer<Long> {
    private final TickHook tickHook;
    private final int collectionPeriod;
    private final Queue<Double> tickDurations;
    private long lastTickTime;
    private double tickDurationSum;
    private final double trimToWithinRange;

    public TpsTracker(TickHook tickHook) {
        this.tickHook = tickHook;
        this.collectionPeriod = 1200;
        this.trimToWithinRange = 50.0 / 100.0;
        tickDurations = new ArrayDeque<>(Collections.nCopies(collectionPeriod, 50.0));
        tickDurationSum = 50.0 * collectionPeriod;
    }


    public void register() {
        tickHook.addTickConsumer(this);
    }

    public synchronized void unregister() {
        tickHook.removeTickConsumer(this);
        tickDurationSum = 0;
        lastTickTime = 0;
        tickDurations.clear();
    }

    private synchronized double getAverageTickTime() {
        return tickDurations.size() < collectionPeriod ? 50.0 : tickDurationSum / (double) collectionPeriod;
    }

    public double getTps() {
        return 1000.0 / getAverageTickTime();
    }

    @Override
    public synchronized void accept(Long value) {
        long curTimeMillis = System.currentTimeMillis();
        double tickDuration = Math.min(Math.max(lastTickTime == 0 ? 50 : (curTimeMillis - lastTickTime), (1.0 - trimToWithinRange) * getAverageTickTime()), (1.0 + trimToWithinRange) * getAverageTickTime());
        tickDurationSum -= tickDurations.remove();
        tickDurations.add(tickDuration);
        tickDurationSum += tickDuration;
        lastTickTime = curTimeMillis;
    }

}
