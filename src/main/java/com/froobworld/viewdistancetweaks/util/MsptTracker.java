package com.froobworld.viewdistancetweaks.util;

import com.froobworld.viewdistancetweaks.hook.tick.TickHook;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class MsptTracker {
    private final Consumer<Integer> tickStartConsumer = this::onTickStart;
    private final Consumer<Integer> tickEndConsumer = this::onTickEnd;
    private final TickHook tickHook;
    private final int collectionPeriod;
    private final Queue<Double> tickDurations;
    private int lastTickNumber;
    private long lastTickStartTime;
    private double tickDurationSum;
    private final double trimToWithinRange;

    public MsptTracker(int collectionPeriod, TickHook tickHook, double trimToWithinRange) {
        this.tickHook = tickHook;
        this.collectionPeriod = collectionPeriod <= 0 ? 1200 : collectionPeriod;
        this.trimToWithinRange = trimToWithinRange / 100.0;
        tickDurations = new ArrayDeque<>();
    }

    public void register() {
        tickHook.addTickStartCallback(tickStartConsumer);
        tickHook.addTickEndCallback(tickEndConsumer);
    }

    public void unregister() {
        tickHook.removeTickStartCallback(tickStartConsumer);
        tickHook.removeTickEndCallback(tickEndConsumer);
        tickDurationSum = 0;
        lastTickStartTime = 0;
        tickDurations.clear();
    }

    public double getMspt() {
        return tickDurations.isEmpty() ? 25.0 : tickDurationSum / (double) tickDurations.size();
    }

    private void onTickStart(int tickNumber) {
        lastTickStartTime = System.currentTimeMillis();
        lastTickNumber = tickNumber;
    }

    private void onTickEnd(int tickNunmber) {
        if (tickNunmber == lastTickNumber) {
            long curTimeMillis = System.currentTimeMillis();
            double tickDuration = Math.max((1.0 - trimToWithinRange) * getMspt(), Math.min((1.0 + trimToWithinRange) * getMspt(), curTimeMillis - lastTickStartTime));
            tickDurationSum += tickDuration - (tickDurations.size() >= collectionPeriod ? tickDurations.remove() : 0);
            tickDurations.add(tickDuration);
        }
    }

}
