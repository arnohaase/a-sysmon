package com.ajjpj.asysmon.data;


import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author arno
 */
public class AHierarchicalData {
    private final boolean isSerial;

    private final long startTimeMillis;
    private final long durationNanos;
    private final String identifier;

    private final Map<String, String> parameters;
    private final List<AHierarchicalData> children;

    /**
     * @param isSerial <code>true</code> designates the intuitive situation that a parent 'contains' several non-overlapping measurements,
     *                     with the sum of the children's durations being less than or equals to the parent's duration and the difference
     *                     being spent in the parent itself.<p>
     *                     <code>false</code> designates more exotic measurements that may 'overlap' other measurements etc., providing
     *                     additional data but not being an additive part of the parent's duration. This could e.g. be the delay between
     *                     sending an asynchronous request and receiving the response.
     * @param identifier is used for aggregated rendering of results - measurements with equal identifiers are treated
     *                   as 'equivalent'.
     */
    public AHierarchicalData(boolean isSerial, long startTimeMillis, long durationNanos, String identifier, Map<String, String> parameters, List<AHierarchicalData> children) {
        this.isSerial = isSerial;
        this.startTimeMillis = startTimeMillis;
        this.durationNanos = durationNanos;
        this.identifier = identifier;
        this.parameters = Collections.unmodifiableMap(parameters);
        this.children = Collections.unmodifiableList(children);
    }

    public boolean isSerial() {
        return isSerial;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<AHierarchicalData> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "AHierarchicalData {" + identifier + " " + parameters + " : " + children + "}";
    }
}
