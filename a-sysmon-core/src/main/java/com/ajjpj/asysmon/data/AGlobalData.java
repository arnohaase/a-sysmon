package com.ajjpj.asysmon.data;


import java.util.Collections;
import java.util.Map;

/**
 * This class represents a number of global data measured at a given point in time.
 *
 * @author arno
 */
public class AGlobalData {
    private final long timestampMillis;

    private final double loadAvgOneMinute;
    private final double loadAvgFiveMinutes;
    private final double loadAvgFifteenMinutes;

    private final int numHttpRequests;
    private final int numOpenDatabaseConnections;

    private final Map<String, Integer> numThreadsByPool;

    public AGlobalData(long timestampMillis, double loadAvgOneMinute, double loadAvgFiveMinutes, double loadAvgFifteenMinutes, int numHttpRequests, int numOpenDatabaseConnections, Map<String, Integer> numThreadsByPool) {
        this.timestampMillis = timestampMillis;
        this.loadAvgOneMinute = loadAvgOneMinute;
        this.loadAvgFiveMinutes = loadAvgFiveMinutes;
        this.loadAvgFifteenMinutes = loadAvgFifteenMinutes;
        this.numHttpRequests = numHttpRequests;
        this.numOpenDatabaseConnections = numOpenDatabaseConnections;
        this.numThreadsByPool = Collections.unmodifiableMap(numThreadsByPool);
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public double getLoadAvgOneMinute() {
        return loadAvgOneMinute;
    }

    public double getLoadAvgFiveMinutes() {
        return loadAvgFiveMinutes;
    }

    public double getLoadAvgFifteenMinutes() {
        return loadAvgFifteenMinutes;
    }

    public int getNumHttpRequests() {
        return numHttpRequests;
    }

    public int getNumOpenDatabaseConnections() {
        return numOpenDatabaseConnections;
    }

    public Map<String, Integer> getNumThreadsByPool() {
        return numThreadsByPool;
    }
}
