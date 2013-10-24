package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ATimer;
import com.ajjpj.asysmon.util.AObjectHolder;
import com.ajjpj.asysmon.util.ArrayStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
        checkNotFinished();

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
            dataSink.onFinishedHierarchicalMeasurement(newData);
        }
        else {
            childrenStack.peek().add(newData);
        }
    }

    @Override public void finish(ASimpleParallelMeasurementImpl m) {
        checkNotFinished();

        final long finishedTimestamp = timer.getCurrentNanos();
        m.getChildrenOfParent().add(new AHierarchicalData(false, m.getStartTimeMillis(), finishedTimestamp - m.getStartTimeNanos(), m.getIdentifier(), m.getParameters(), Collections.<AHierarchicalData>emptyList()));
    }

    @Override
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean isSerial) {
        checkNotFinished();
        if(unfinished.isEmpty()) {
            throw new IllegalStateException("currently no support for top-level collecting measurements"); //TODO what is a good way to get around this?
        }

        return new ACollectingMeasurement(timer, this, isSerial, identifier, childrenStack.peek());
    }

    @Override public void finish(ACollectingMeasurement m) {
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
}

//TODO limit stack depth - if deeper than limit, discard and print error message --> prevent memory leak!
//TODO mechanism for applications to say 'we *should* be finished now'