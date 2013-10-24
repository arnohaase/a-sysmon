package com.ajjpj.asysmon.processing.minmaxavg;


import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arno
 */
public class AMinMaxAvgData {
    private final boolean isDisjoint;
    private final int totalNumInContext;
    private final long minNanos;
    private final long maxNanos;
    private final long avgNanos;
    private final long totalNanos;

    private final ConcurrentHashMap<String, AMinMaxAvgData> children;

    public AMinMaxAvgData(boolean isDisjoint, long initialNanos) {
        this(isDisjoint, 1, initialNanos, initialNanos, initialNanos, initialNanos, new ConcurrentHashMap<String, AMinMaxAvgData>());
    }

    public AMinMaxAvgData(boolean isDisjoint, int totalNumInContext, long minNanos, long maxNanos, long avgNanos, long totalNanos, ConcurrentHashMap<String, AMinMaxAvgData> children) {
        this.isDisjoint = isDisjoint;
        this.totalNumInContext = totalNumInContext;
        this.minNanos = minNanos;
        this.maxNanos = maxNanos;
        this.avgNanos = avgNanos;
        this.totalNanos = totalNanos;
        this.children = children;
    }

    public AMinMaxAvgData withDataPoint(boolean isDisjoint, long durationNanos) {
        if(isDisjoint != this.isDisjoint) {
            throw new IllegalArgumentException("both parallel and serial measurements at the same level with the same identifier");
        }

        return new AMinMaxAvgData(isDisjoint,
                totalNumInContext+1,
                Math.min(minNanos, durationNanos),
                Math.max(maxNanos, durationNanos),
                (avgNanos * totalNumInContext + durationNanos) / (totalNumInContext + 1), // this should be safe against overflow
                totalNanos + durationNanos,
                children);
    }

    public boolean isDisjoint() {
        return isDisjoint;
    }

    public long getTotalNanos() {
        return totalNanos;
    }

    public int getTotalNumInContext() {
        return totalNumInContext;
    }

    public long getMinNanos() {
        return minNanos;
    }

    public long getMaxNanos() {
        return maxNanos;
    }

    public long getAvgNanos() {
        return avgNanos;
    }

    public ConcurrentHashMap<String, AMinMaxAvgData> getChildren() {
        return children;
    }
}
