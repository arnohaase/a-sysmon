package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.AHierarchicalData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author arno
 */
class ASimpleParallelMeasurementImpl implements ASimpleMeasurement {
    private static final ASysMonLogger log = ASysMonLogger.get(ASimpleParallelMeasurementImpl.class);

    private final long startTimeMillis = System.currentTimeMillis();
    private final long startTimeNanos; // this number has no absolute meaning and is useful only for measuring differences
    private final String identifier;

    private final Map<String, String> parameters = new TreeMap<String, String>();

    private final AMeasurementHierarchy hierarchy;
    private final List<AHierarchicalData> childrenOfParent;

    private boolean isFinished = false;

    ASimpleParallelMeasurementImpl(AMeasurementHierarchy hierarchy, long startTimeNanos, String identifier, List<AHierarchicalData> childrenOfParent) {
        this.hierarchy = hierarchy;
        this.startTimeNanos = startTimeNanos;
        this.identifier = identifier;
        this.childrenOfParent = childrenOfParent;
    }

    @Override public void addParameter(String identifier, String value) {
        if(parameters.put(identifier, value) != null) {
            log.warn("duplicate parameter " + identifier + " for a measurement");
        }
    }

    public void finish() {
        if(isFinished) {
            throw new IllegalStateException("a simple measurement can be finished only once.");
        }

        isFinished = true;
        hierarchy.finish(this);
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

    List<AHierarchicalData> getChildrenOfParent() {
        return childrenOfParent;
    }
}
