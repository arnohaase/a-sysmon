package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.AHierarchicalData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * This is a measurement that aggregates several - potentially many - measurements in a single object. It is intended
 *  for situations that have interesting details but do not warrant full-blown child measurements.<p>
 * A typical example of this is iterating over a JDBC ResultSet: calling 'next()' on the ResultSet may take
 *  significant time, and measuring that time can be of interest. It is however often sufficient to measure the
 *  total time used up by iterating over the ResultSet.
 *
 * @author arno
 */
public class ACollectingMeasurement implements AWithParameters {
    private static final ASysMonLogger log = ASysMonLogger.get(ACollectingMeasurement.class);

    private final ASysMonConfig config;
    private final AMeasurementHierarchy hierarchy;
    private final boolean isSerial;

    private final long startTimeMillis = System.currentTimeMillis();
    private final String identifier;

    private final Map<String, String> parameters = new TreeMap<String, String>();
    private final Map<String, Detail> details = new TreeMap<String, Detail>();

    private final List<AHierarchicalData> childrenOfParent;

    private long totalDurationNanos = 0;

    private String detailIdentifier = null;
    private long detailStartTimeNanos; // this number has no absolute meaning and is useful only for measuring differences

    private boolean isFinished = false;

    public ACollectingMeasurement(ASysMonConfig config, AMeasurementHierarchy hierarchy, boolean isSerial, String identifier, List<AHierarchicalData> childrenOfParent) {
        this.config = config;
        this.hierarchy = hierarchy;
        this.isSerial = isSerial;
        this.identifier = identifier;
        this.childrenOfParent = childrenOfParent;
    }

    List<AHierarchicalData> getChildrenOfParent() {
        return childrenOfParent;
    }

    public boolean isSerial() {
        return isSerial;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override public void addParameter(String identifier, String value) {
        if(parameters.put(identifier, value) != null) {
            log.warn("duplicate parameter " + identifier);
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, Detail> getDetails() {
        return details;
    }

    public long getTotalDurationNanos() {
        return totalDurationNanos;
    }

    public <R, E extends Exception> R detail(String detailIdentifier, AMeasureCallback<R,E> callback) throws E {
        startDetail(detailIdentifier);
        try {
            return callback.call(this);
        } finally {
            finishDetail();
        }
    }

    public void startDetail(String detailIdentifier) {
        if(config.isGloballyDisabled()) {
            return;
        }

        if(this.detailIdentifier != null) {
            throw new IllegalStateException("a detail measurement is already running");
        }

        this.detailIdentifier = detailIdentifier;
        detailStartTimeNanos = config.timer.getCurrentNanos();
    }

    public void finishDetail() {
        if(config.isGloballyDisabled()) {
            return;
        }

        if(this.detailIdentifier == null) {
            throw new IllegalStateException("no current detail measurement");
        }

        final long duration = config.timer.getCurrentNanos() - detailStartTimeNanos;
        addDetailMeasurement(detailIdentifier, duration);
        detailIdentifier = null;
    }

    public void addDetailMeasurement(String detailIdentifier, long durationNanos) {
        if(config.isGloballyDisabled()) {
            return;
        }

        totalDurationNanos += durationNanos;

        final Detail prev = details.get(detailIdentifier);
        if(prev == null) {
            details.put(detailIdentifier, new Detail(durationNanos));
        }
        else {
            prev.addMeasurement(durationNanos);
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void finish() {
        if(config.isGloballyDisabled()) {
            return;
        }

        if(isFinished) {
            return; // TODO the statement may have been implicitly closed by now - how to handle this best? --> was 'throw new IllegalStateException("a measurement can be finished only once.");'
        }
        if(detailIdentifier != null) {
            log.warn("unfinished detail - finishing implicitly");
            // finish the last detail implicitly
            finishDetail();
        }


        isFinished = true;
        hierarchy.finish(this);
    }

    public static class Detail {
        private long totalNanos;
        private int num;

        public Detail(long nanos) {
            this(nanos, 1);
        }

        private Detail(long totalNanos, int num) {
            this.totalNanos = totalNanos;
            this.num = num;
        }

        public void addMeasurement(long nanos) {
            totalNanos += nanos;
            num += 1;
        }

        public long getTotalNanos() {
            return totalNanos;
        }

        public int getNum() {
            return num;
        }
    }
}
