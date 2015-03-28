package com.ajjpj.asysmon.measure;

import com.ajjpj.afoundation.collection.mutable.ArrayStack;
import com.ajjpj.afoundation.function.AFunction0NoThrow;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.*;


/**
 * This class collects a tree of hierarchical measurements. It lives in a single thread.
 *
 * @author arno
 */
public class AMeasurementHierarchyImpl implements AMeasurementHierarchy {
    private static final ASysMonLogger log = ASysMonLogger.get(AMeasurementHierarchyImpl.class);

    private final ASysMonConfig config;
    private final ADataSink dataSink;

    private Set<ACollectingMeasurement> collectingMeasurements = new HashSet<>();

    private int size = 0; // total number of measurements in this hierarchy

    private final ArrayStack<ASimpleSerialMeasurementImpl> unfinished = new ArrayStack<>();
    private final ArrayStack<List<AHierarchicalData>> childrenStack = new ArrayStack<>();

    private final Collection<ACorrelationId> startedFlows = new HashSet<>();
    private final Collection<ACorrelationId> joinedFlows = new HashSet<>();

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

    private boolean checkNotFinished () {
        if(isFinished) {
            log.error (new IllegalStateException("This measurement is already closed."));
        }
        return isFinished;
    }

    @Override public ASimpleMeasurement start(String identifier, boolean isSerial) {
        if(config.isGloballyDisabled() || checkNotFinished()) {
            return new ASimpleMeasurement() {
                @Override public void finish() {
                }

                @Override public void addParameter(String identifier, String value) {
                }
            };
        }

        if(unfinished.isEmpty()) {
            dataSink.onStartedHierarchicalMeasurement(identifier);
        }

        if(isSerial) {
            checkOverflow();
            final ASimpleSerialMeasurementImpl result = new ASimpleSerialMeasurementImpl(this, config.timer.getCurrentNanos(), identifier);
            unfinished.push(result);
            size += 1;
            childrenStack.push(new ArrayList<AHierarchicalData>());
            return result;
        }
        else {
            return new ASimpleParallelMeasurementImpl(this, config.timer.getCurrentNanos(), identifier, childrenStack.peek());
        }
    }

    private void checkOverflow() {
        checkMaxDepth ();
        checkMaxSize ();
    }

    private void checkMaxSize () {
        if (wasKilled || size < config.maxNumMeasurementsPerHierarchy) {
            return;
        }

        final ASimpleSerialMeasurementImpl rootMeasurement = doKillForcefully ();
        log.error ("Excessive number of measurements in a single hierarchy:  " + size + " - probable memory leak, forcefully cleaning measurement stack. Root measurement was " +
                rootMeasurement.getIdentifier() + " with parameters " + rootMeasurement.getParameters() + ", started at " + new Date(rootMeasurement.getStartTimeMillis()));
    }

    private ASimpleSerialMeasurementImpl doKillForcefully() {
        ASimpleSerialMeasurementImpl rootMeasurement = null;
        while(unfinished.nonEmpty()) {
            rootMeasurement = unfinished.peek();
            finish (unfinished.peek());
        }

        return rootMeasurement;
    }

    private void checkMaxDepth () {
        if (wasKilled || unfinished.size() < config.maxNestedMeasurements) {
            return;
        }

        final ASimpleSerialMeasurementImpl rootMeasurement = doKillForcefully ();
        log.error ("Call depth " + unfinished.size () + " - probable memory leak, forcefully cleaning measurement stack. Root measurement was " +
                rootMeasurement.getIdentifier() + " with parameters " + rootMeasurement.getParameters() + ", started at " + new Date(rootMeasurement.getStartTimeMillis()));
    }

    private void logWasKilled() {
        log.debug (new AFunction0NoThrow<String> () {
            @Override public String apply () {
                return "Interacting with a forcefully killed measurement. This is a consequence of A-SysMon cleaning up a (suspected) memory leak. It has no consequences aside from potentially weird measurements being reported.";
            }
        });
    }

    @Override public void finish(ASimpleSerialMeasurementImpl measurement) {
        if(ASysMonConfig.isGloballyDisabled()) {
            return;
        }

        if(wasKilled) {
            logWasKilled();
            return;
        }
        if (checkNotFinished ()) {
            return;
        }

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
                log.error (new IllegalStateException("Calling 'finish' on a measurement that is not on the measurement stack: " + measurement));
                return;
            }
        }

        final long finishedTimestamp = config.timer.getCurrentNanos();

        unfinished.pop();
        final List<AHierarchicalData> children = childrenStack.pop();
        final AHierarchicalData newData = new AHierarchicalData(true, measurement.getStartTimeMillis(), finishedTimestamp - measurement.getStartTimeNanos(), measurement.getIdentifier(), measurement.getParameters(), children);

        if(unfinished.isEmpty()) {
            // copy into a separate collection because the collection is modified in the loop
            for(ACollectingMeasurement m: new ArrayList<>(collectingMeasurements)) {
                finish(m);
            }
            isFinished = true;
            dataSink.onFinishedHierarchicalMeasurement(new AHierarchicalDataRoot(newData, startedFlows, joinedFlows));
        }
        else {
            childrenStack.peek().add(newData);
        }
    }

    @Override public void finish(ASimpleParallelMeasurementImpl m) {
        if(ASysMonConfig.isGloballyDisabled()) {
            return;
        }

        if(wasKilled) {
            logWasKilled();
            return;
        }
        if (checkNotFinished ()) {
            return;
        }

        final long finishedTimestamp = config.timer.getCurrentNanos();
        m.getChildrenOfParent().add(new AHierarchicalData(false, m.getStartTimeMillis(), finishedTimestamp - m.getStartTimeNanos(), m.getIdentifier(), m.getParameters(), Collections.<AHierarchicalData>emptyList()));
    }

    @Override
    public ACollectingMeasurement startCollectingMeasurement (final String identifier, boolean isSerial) {
        if(ASysMonConfig.isGloballyDisabled() || checkNotFinished()) {
            return ACollectingMeasurement.createDisabled ();
        }

        if(unfinished.isEmpty()) {
            // A collection measurement can never be top-level. To be on the safe side, we just ignore this, losing measurement data rather than risking non-robust code.
            // Declarative transactions can cause this if the start and especially the end of a transaction are not surrounded by an ASysMon measurement
            log.debug (new AFunction0NoThrow<String> () {
                @Override public String apply () {
                    return "Trying to start a collectiong mesaurement outside of a measurement hierarchy: " + identifier;
                }
            });
            return ACollectingMeasurement.createDisabled ();
        }
        else {
            final ACollectingMeasurement result = ACollectingMeasurement.createRegular (config, this, isSerial, identifier, childrenStack.peek());
            collectingMeasurements.add(result);
            size += 1;
            return result;
        }
    }

    @Override public void finish(ACollectingMeasurement m) {
        if(ASysMonConfig.isGloballyDisabled()) {
            return;
        }

        if(wasKilled) {
            logWasKilled();
            return;
        }
        if (checkNotFinished ()) {
            return;
        }

        final List<AHierarchicalData> children = new ArrayList<AHierarchicalData>();
        for(String detailIdentifier: m.getDetails().keySet()) {
            final ACollectingMeasurement.Detail detail = m.getDetails().get(detailIdentifier);
            children.add(new AHierarchicalData(true, m.getStartTimeMillis(), detail.getTotalNanos(), detailIdentifier, Collections.<String, String>emptyMap(), Collections.<AHierarchicalData>emptyList()));
        }

        final AHierarchicalData newData = new AHierarchicalData(m.isSerial(), m.getStartTimeMillis(), m.getTotalDurationNanos(), m.getIdentifier(), m.getParameters(), children);
        m.getChildrenOfParent().add(newData);
        collectingMeasurements.remove(m);
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

