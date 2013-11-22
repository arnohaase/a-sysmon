package com.ajjpj.asysmon.measure.threadpool;

import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.global.AScalarMeasurer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public class AThreadCountMeasurer implements AScalarMeasurer {
    public final AThreadPoolTrackingDataSink counter = new AThreadPoolTrackingDataSink();

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp) {
        for(Map.Entry<String, AtomicInteger> entry: counter.getThreadCounts().entrySet()) {
            final String ident = "Thread Pool " + entry.getKey();
            data.put(ident, new AScalarDataPoint(timestamp, ident, entry.getValue().get(), 0));
        }
    }

    @Override public void shutdown() {
    }
}
