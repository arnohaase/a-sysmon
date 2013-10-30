package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.util.timer.ATimer;
import com.ajjpj.asysmon.util.AObjectHolder;
import com.ajjpj.asysmon.util.ArrayStack;

import java.util.*;


/**
 * This class collects a tree of hierarchical measurements, i.e. it lives in a single thread.
 *
 * @author arno
 */
public class AMeasurementHierarchyImpl implements AMeasurementHierarchy {
    private final ATimer timer;
    private final ADataSink dataSink;

    private final ArrayStack<ASimpleSerialMeasurementImpl> unfinished = new ArrayStack<ASimpleSerialMeasurementImpl>();
    private final ArrayStack<List<AHierarchicalData>> childrenStack = new ArrayStack<List<AHierarchicalData>>();

    private final Collection<ACorrelationId> startedFlows = new HashSet<ACorrelationId>();
    private final Collection<ACorrelationId> joinedFlows = new HashSet<ACorrelationId>();

    private final AObjectHolder<Boolean> isFinished = new AObjectHolder<Boolean>(Boolean.FALSE);

    public AMeasurementHierarchyImpl(ATimer timer, ADataSink dataSink) {
        this.timer = timer;
        this.dataSink = dataSink;
    }

    private void checkNotFinished() {
        if(isFinished.value) {
            throw new IllegalStateException("measurements must not be reused - this measurement is already closed");
        }
    }

    @Override public ASimpleMeasurement start(String identifier, boolean isSerial) {
        if(AStaticSysMonConfig.isGloballyDisabled()) {
            return new ASimpleMeasurement() {
                @Override public void finish() {
                }

                @Override public void addParameter(String identifier, String value) {
                }
            };
        }

        checkNotFinished();

        if(unfinished.isEmpty()) {
            dataSink.onStartedHierarchicalMeasurement();
        }

        if(isSerial) {
            final ASimpleSerialMeasurementImpl result = new ASimpleSerialMeasurementImpl(this, timer.getCurrentNanos(), identifier);
            unfinished.push(result);
            childrenStack.push(new ArrayList<AHierarchicalData>());
            return result;
        }
        else {
            return new ASimpleParallelMeasurementImpl(this, timer.getCurrentNanos(), identifier, childrenStack.peek());
        }
    }

    @Override public void finish(ASimpleSerialMeasurementImpl measurement) {
        if(AStaticSysMonConfig.isGloballyDisabled()) {
            return;
        }

        checkNotFinished();

        if (unfinished.peek() != measurement) {
            //TODO this is a bug in using code - how to deal with it?!
            throw new IllegalStateException("measurements must be strictly nested");
        }

        final long finishedTimestamp = timer.getCurrentNanos();

        unfinished.pop();
        final List<AHierarchicalData> children = childrenStack.pop();
        final AHierarchicalData newData = new AHierarchicalData(true, measurement.getStartTimeMillis(), finishedTimestamp - measurement.getStartTimeNanos(), measurement.getIdentifier(), measurement.getParameters(), children);

        if(unfinished.isEmpty()) {
            isFinished.value = true;
            dataSink.onFinishedHierarchicalMeasurement(newData, startedFlows, joinedFlows);
        }
        else {
            childrenStack.peek().add(newData);
        }
    }

    @Override public void finish(ASimpleParallelMeasurementImpl m) {
        if(AStaticSysMonConfig.isGloballyDisabled()) {
            return;
        }

        checkNotFinished();

        final long finishedTimestamp = timer.getCurrentNanos();
        m.getChildrenOfParent().add(new AHierarchicalData(false, m.getStartTimeMillis(), finishedTimestamp - m.getStartTimeNanos(), m.getIdentifier(), m.getParameters(), Collections.<AHierarchicalData>emptyList()));
    }

    @Override
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean isSerial) {
        if(AStaticSysMonConfig.isGloballyDisabled()) {
            return new ACollectingMeasurement(null, null, true, null, null);
        }

        checkNotFinished();
        if(unfinished.isEmpty()) {
            throw new IllegalStateException("currently no support for top-level collecting measurements"); //TODO what is a good way to get around this?
        }

        return new ACollectingMeasurement(timer, this, isSerial, identifier, childrenStack.peek());
    }

    @Override public void finish(ACollectingMeasurement m) {
        if(AStaticSysMonConfig.isGloballyDisabled()) {
            return;
        }

        checkNotFinished();

        final List<AHierarchicalData> children = new ArrayList<AHierarchicalData>();
        for(String detailIdentifier: m.getDetails().keySet()) {
            final ACollectingMeasurement.Detail detail = m.getDetails().get(detailIdentifier);
            //TODO how to store m.getNum()?
            children.add(new AHierarchicalData(true, m.getStartTimeMillis(), detail.getTotalNanos(), detailIdentifier, Collections.<String, String>emptyMap(), Collections.<AHierarchicalData>emptyList()));
        }

        final AHierarchicalData newData = new AHierarchicalData(m.isSerial(), m.getStartTimeMillis(), m.getTotalDurationNanos(), m.getIdentifier(), m.getParameters(), children);
        m.getChildrenOfParent().add(newData);
    }

    /**
     * notifies that this measurements contains the start of a new 'flow', i.e. it is the first point in a (potential) chain
     *  of measurements that are somehow correlated.
     */
    public void onStartFlow(ACorrelationId correlationId) {
        //TODO warn duplicates - in both collections
        startedFlows.add(correlationId);
    }

    /**
     * notifies that this measurements is part of an existing 'flow', i.e. there is another measurement that 'started' a
     *  set of measurements that are somehow correlated.
     */
    public void onJoinFlow(ACorrelationId correlationId) {
        //TODO warn duplicates - in both collections
        joinedFlows.add(correlationId);
    }
}

//TODO limit stack depth - if deeper than limit, discard and print error message --> prevent memory leak!
//TODO mechanism for applications to say 'we *should* be finished now'