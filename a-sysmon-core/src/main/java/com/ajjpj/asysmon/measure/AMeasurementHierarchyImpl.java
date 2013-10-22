package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ATimer;
import com.ajjpj.asysmon.util.ArrayStack;

import java.util.ArrayList;
import java.util.List;


/**
 * This class collects a tree of hierarchical measurements, i.e. it lives in a single thread.
 *
 * @author arno
 */
public class AMeasurementHierarchyImpl implements com.ajjpj.asysmon.measure.AMeasurementHierarchy {
    private final ATimer timer;
    private final ADataSink dataSink;

    private final ArrayStack<ASimpleMeasurement> unfinished = new ArrayStack<com.ajjpj.asysmon.measure.ASimpleMeasurement>();
    private final ArrayStack<List<AHierarchicalData>> childrenStack = new ArrayStack<List<AHierarchicalData>>();

    private boolean isFinished = false;

    public AMeasurementHierarchyImpl(ATimer timer, ADataSink dataSink) {
        this.timer = timer;
        this.dataSink = dataSink;
    }

    @Override public ASimpleMeasurement start(String identifier, boolean disjoint) {
        final ASimpleMeasurement result = new ASimpleMeasurement(this, disjoint, timer.getCurrentNanos(), identifier);
        unfinished.push(result);
        childrenStack.push(new ArrayList<AHierarchicalData>());
        return result;
    }

    @Override public void finish(ASimpleMeasurement measurement) {
        if(isFinished) {
            throw new IllegalStateException("measurements must not be reused - this measurement is already closed");
        }
        isFinished = true;

        if (unfinished.peek() != measurement) {
            //TODO this is a bug in using code - how to deal with it?!
            throw new IllegalStateException("measurements must be strictly nested");
        }

        final long finishedTimestamp = timer.getCurrentNanos();

        unfinished.pop();
        final List<AHierarchicalData> children = childrenStack.pop();
        final AHierarchicalData newData = new AHierarchicalData(measurement.isDisjoint(), measurement.getStartTimeMillis(), finishedTimestamp - measurement.getStartTimeNanos(), measurement.getIdentifier(), measurement.getParameters(), children);

        if(unfinished.isEmpty()) {
            dataSink.onFinishedHierarchicalData(newData);
        }
        else {
            childrenStack.peek().add(newData);
        }
    }
}

//TODO limit stack depth - if deeper than limit, discard and print error message --> prevent memory leak!
//TODO mechanism for applications to say 'we *should* be finished now'