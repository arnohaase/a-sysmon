package com.ajjpj.asysmon.measure;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a builder class representing an ongoing thread specific measurement.
 *
 * @author arno
 */
public class ASimpleMeasurement implements AWithParameters {
    private final AMeasurementHierarchy hierarchy;
    private final boolean disjoint;

    private final long startTimeMillis = System.currentTimeMillis();
    private final long startTimeNanos; // this number has no absolute meaning and is useful only for measuring differences
    private final String identifier;

    private final Map<String, String> parameters = new HashMap<String, String>();

    private boolean isFinished = false;

    ASimpleMeasurement(AMeasurementHierarchy hierarchy, boolean disjoint, long startTimeNanos, String identifier) {
        this.hierarchy = hierarchy;
        this.disjoint = disjoint;
        this.startTimeNanos = startTimeNanos;
        this.identifier = identifier;
    }

    @Override public void addParameter(String identifier, String value) {
        parameters.put(identifier, value); //TODO warn of duplicates?
    }

    public void finish() {
        if(isFinished) {
            throw new IllegalStateException("a simple measurement can be finished only once.");
        }

        isFinished = true;
        hierarchy.finish(this);
    }

    public boolean isDisjoint() {
        return disjoint;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
