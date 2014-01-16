package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.util.ArrayStack;

import java.util.*;


/**
 * This class collects a tree of hierarchical measurements. It lives in a single thread.
 *
 * @author arno
 */
public class AMeasurementHierarchyImpl implements AMeasurementHierarchy {
    /**
     * This is the number of nested measurements currently supported - when this level is reached, A-SysMon assumes
     *  there is a memory leak (i.e. there are measurements that are started but never finished) and kills the
     *  measurement hierarchy.
     */
    public static final int MAX_CALL_DEPTH = 1000;

    private static final ASysMonLogger log = ASysMonLogger.get(AMeasurementHierarchyImpl.class);

    private final ASysMonConfig config;
    private final ADataSink dataSink;

    private Set<ACollectingMeasurement> collectingMeasurements = new HashSet<ACollectingMeasurement>();

    private boolean hasSyntheticRoot = false;

    private final ArrayStack<ASimpleSerialMeasurementImpl> unfinished = new ArrayStack<ASimpleSerialMeasurementImpl>();
    private final ArrayStack<List<AHierarchicalData>> childrenStack = new ArrayStack<List<AHierarchicalData>>();

    private final Collection<ACorrelationId> startedFlows = new HashSet<ACorrelationId>();
    private final Collection<ACorrelationId> joinedFlows = new HashSet<ACorrelationId>();

    /**
     * shows if this measurement was finished in an orderly fashion
     */
    private boolean isFinished = false;

    /**
     * shows if this measurement was killed forcibly e.g. because there was an overflow on the stack
     */
    private boolean wasKilled = false;

    public AMeasurementHierarchyImpl(ASysMonConfig config, ADataSink dataSink) {
        this.config = config;
        this.dataSink = dataSink;
    }

    private void checkNotFinished() {
        if(isFinished) {
            throw new IllegalStateException("This measurement is already closed.");
        }
    }

    @Override public ASimpleMeasurement start(String identifier, boolean isSerial) {
        if(config.isGloballyDisabled()) {
            return new ASimpleMeasurement() {
                @Override public void finish() {
                }

                @Override public void addParameter(String identifier, String value) {
                }
            };
        }

        checkNotFinished();

        if(unfinished.isEmpty()) {
            dataSink.onStartedHierarchicalMeasurement(identifier);
        }

        if(isSerial) {
            checkOverflow();
            final ASimpleSerialMeasurementImpl result = new ASimpleSerialMeasurementImpl(this, config.timer.getCurrentNanos(), identifier);
            unfinished.push(result);
            childrenStack.push(new ArrayList<AHierarchicalData>());
            return result;
        }
        else {
            return new ASimpleParallelMeasurementImpl(this, config.timer.getCurrentNanos(), identifier, childrenStack.peek());
        }
    }

    private void checkOverflow() {
        if(unfinished.size() < MAX_CALL_DEPTH) {
            return;
        }

        ASimpleSerialMeasurementImpl rootMeasurement = null;
        while(unfinished.nonEmpty()) {
            rootMeasurement = unfinished.peek();
            finish(unfinished.peek());
        }

        log.error("Detected probably memory leak, forcefully cleaning measurement stack. Root measurement was " + rootMeasurement.getIdentifier() + " with parameters " + rootMeasurement.getParameters() + ", started at " + new Date(rootMeasurement.getStartTimeMillis()));
        wasKilled = true;
    }

    private void logWasKilled() {
        log.warn("Interacting with a forcefully killed measurement. This is a consequence of A-SysMon cleaning up a (suspected) memory leak. It has no consequences aside from potentially weird measurements being reported.");
    }

    @Override public void finish(ASimpleSerialMeasurementImpl measurement) {
        if(config.isGloballyDisabled()) {
            return;
        }

        if(wasKilled) {
            logWasKilled();
            return;
        }
        checkNotFinished();

        if (unfinished.peek() != measurement) {
            // This is basically a bug in using code: a measurement is 'finished' without being the innermost measurement
            //  of this hierarchy. The most typical reason for this - and the only one we can recover from - is that
            //  using code skipped finishing an inner measurement and is now finishing something further outside.


            if(unfinished.contains(measurement)) {
                log.warn("Calling 'finish' on a measurement " + measurement + " that is not innermost on the stack.");

                while(unfinished.peek() != measurement) {
                    log.warn("-> Implicitly unrolling the stack of open measurements: " + unfinished.peek());
                    finish(unfinished.peek());
                }
            }
            else {
                throw new IllegalStateException("Calling 'finish' on a measurement that is not on the measurement stack: " + measurement);
            }
        }

        final long finishedTimestamp = config.timer.getCurrentNanos();

        unfinished.pop();
        final List<AHierarchicalData> children = childrenStack.pop();
        final AHierarchicalData newData = new AHierarchicalData(true, measurement.getStartTimeMillis(), finishedTimestamp - measurement.getStartTimeNanos(), measurement.getIdentifier(), measurement.getParameters(), children);

        if(unfinished.isEmpty()) {
            // copy into a separate collection because the collection is modified in the loop
            for(ACollectingMeasurement m: new ArrayList<ACollectingMeasurement>(collectingMeasurements)) {
                finish(m);
            }
            isFinished = true;
            dataSink.onFinishedHierarchicalMeasurement(new AHierarchicalDataRoot(newData, startedFlows, joinedFlows));
        }
        else {
            childrenStack.peek().add(newData);
        }
        checkFinishSyntheticRoot();
    }

    @Override public void finish(ASimpleParallelMeasurementImpl m) {
        if(config.isGloballyDisabled()) {
            return;
        }

        if(wasKilled) {
            logWasKilled();
            return;
        }
        checkNotFinished();

        final long finishedTimestamp = config.timer.getCurrentNanos();
        m.getChildrenOfParent().add(new AHierarchicalData(false, m.getStartTimeMillis(), finishedTimestamp - m.getStartTimeNanos(), m.getIdentifier(), m.getParameters(), Collections.<AHierarchicalData>emptyList()));
        checkFinishSyntheticRoot();
    }

    @Override
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean isSerial) {
        if(config.isGloballyDisabled()) {
            return new ACollectingMeasurement(null, null, true, null, null);
        }

        checkNotFinished();
        if(unfinished.isEmpty()) {
            start(IDENT_SYNTHETIC_ROOT, true);
            hasSyntheticRoot = true;
        }

        final ACollectingMeasurement result = new ACollectingMeasurement(config, this, isSerial, identifier, childrenStack.peek());
        collectingMeasurements.add(result);
        return result;
    }

    @Override public void finish(ACollectingMeasurement m) {
        if(config.isGloballyDisabled()) {
            return;
        }

        if(wasKilled) {
            logWasKilled();
            return;
        }
        checkNotFinished();

        final List<AHierarchicalData> children = new ArrayList<AHierarchicalData>();
        for(String detailIdentifier: m.getDetails().keySet()) {
            final ACollectingMeasurement.Detail detail = m.getDetails().get(detailIdentifier);
            children.add(new AHierarchicalData(true, m.getStartTimeMillis(), detail.getTotalNanos(), detailIdentifier, Collections.<String, String>emptyMap(), Collections.<AHierarchicalData>emptyList()));
        }

        final AHierarchicalData newData = new AHierarchicalData(m.isSerial(), m.getStartTimeMillis(), m.getTotalDurationNanos(), m.getIdentifier(), m.getParameters(), children);
        m.getChildrenOfParent().add(newData);
        collectingMeasurements.remove(m);
        checkFinishSyntheticRoot();
    }

    private void checkFinishSyntheticRoot() {
        if(hasSyntheticRoot &&
                collectingMeasurements.isEmpty() &&
                unfinished.size() == 1) {
            finish(unfinished.peek());
        }
    }

    /**
     * notifies that this measurements contains the start of a new 'flow', i.e. it is the first point in a (potential) chain
     *  of measurements that are somehow correlated.
     */
    @Override public void onStartFlow(ACorrelationId correlationId) {
        if(!startedFlows.add(correlationId)) {
            log.warn("called 'startFlow' for flow " + correlationId + " twice");
        }
    }

    /**
     * notifies that this measurements is part of an existing 'flow', i.e. there is another measurement that 'started' a
     *  set of measurements that are somehow correlated.
     */
    @Override public void onJoinFlow(ACorrelationId correlationId) {
        if(!joinedFlows.add(correlationId)) {
            log.warn("called 'joinFlow' for flow " + correlationId + " twice");
        }
    }
}

