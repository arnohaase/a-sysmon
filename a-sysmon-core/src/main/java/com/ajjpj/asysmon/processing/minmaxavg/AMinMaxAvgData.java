package com.ajjpj.asysmon.processing.minmaxavg;


import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arno
 */
public class AMinMaxAvgData {
    private final int totalNumInContext;
    private final long minNanos;
    private final long maxNanos;
    private final long avgNanos;

    private final ConcurrentHashMap<String, AMinMaxAvgData> children;

    public AMinMaxAvgData(long initialNanos) {
        this(1, initialNanos, initialNanos, initialNanos, new ConcurrentHashMap<String, AMinMaxAvgData>());
    }

    private AMinMaxAvgData(int totalNumInContext, long minNanos, long maxNanos, long avgNanos, ConcurrentHashMap<String, AMinMaxAvgData> children) {
        this.totalNumInContext = totalNumInContext;
        this.minNanos = minNanos;
        this.maxNanos = maxNanos;
        this.avgNanos = avgNanos;
        this.children = children;
    }

    public AMinMaxAvgData withDataPoint(long durationNanos) {
        return new AMinMaxAvgData(totalNumInContext+1,
                Math.min(minNanos, durationNanos),
                Math.max(maxNanos, durationNanos),
                (avgNanos * totalNumInContext + durationNanos) / (totalNumInContext + 1), // this should be safe against overflow
                children);
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
