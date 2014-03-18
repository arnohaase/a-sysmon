package com.ajjpj.asysmon.servlet.trace;

import com.ajjpj.abase.collection.mutable.ARingBuffer;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;


/**
 * @author arno
 */
class ATraceCollectingDataSink implements ADataSink {
    public boolean isStarted = true;
    private final ATraceFilter traceFilter;
    private final ARingBuffer<AHierarchicalDataRoot> data;

    ATraceCollectingDataSink(ATraceFilter traceFilter, int bufferSize) {
        this.traceFilter = traceFilter;
        this.data = new ARingBuffer<AHierarchicalDataRoot>(AHierarchicalDataRoot.class, bufferSize);
    }

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot trace) {
        if(isStarted && traceFilter.shouldCollect(trace)) {
            this.data.put(trace);
        }
    }

    @Override public void shutdown() throws Exception {
    }

    public void clear() {
        data.clear();
    }

    public Iterable<AHierarchicalDataRoot> getData() {
        return data;
    }
}
