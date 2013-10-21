package com.ajjpj.asysmon.data;


import java.util.Collections;
import java.util.Map;


/**
 * @author arno
 */
public class AThreadBasedData {
    private final AThreadBasedData parent;

    private final boolean isPartOfParent;

    private final long startTimeMillis;
    private final long durationNanos;
    private final String identifier;

    private final Map<String, String> parameters;

    /**
     * @param parent defines the hierarchy of measurements, the idea being that a parent's duration contains the the durations
     *               of its children, with children not overlapping. <code>null</code> means this is the root (for a given thread).
     * @param partOfParent <code>true</code> designates the intuitive situation that a parent 'contains' several non-overlapping measurements,
     *                     with the sum of the children's durations being less than or equals to the parent's duration and the difference
     *                     being spent in the parent itself.<p />
     *                     <code>false</code> designates more exotic measurements that may 'overlap' other measurements etc., providing
     *                     additional data but not being an additive part of the parent's duration. This could e.g. be the delay between
     *                     sending an asynchronous request and receiving the response.
     * @param identifier is used for aggregated rendering of results - measurements with equal identifiers are treated
     *                   as 'equivalent'.
     */
    public AThreadBasedData(AThreadBasedData parent, boolean partOfParent, long startTimeMillis, long durationNanos, String identifier, Map<String, String> parameters) {
        this.parent = parent;
        isPartOfParent = partOfParent;
        this.startTimeMillis = startTimeMillis;
        this.durationNanos = durationNanos;
        this.identifier = identifier;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public AThreadBasedData getParent() {
        return parent;
    }

    public boolean isPartOfParent() {
        return isPartOfParent;
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
}
