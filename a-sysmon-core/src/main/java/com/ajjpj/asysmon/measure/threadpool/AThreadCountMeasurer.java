package com.ajjpj.asysmon.measure.threadpool;

import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public class AThreadCountMeasurer implements AGlobalMeasurer {
    public final AThreadPoolTrackingDataSink counter = new AThreadPoolTrackingDataSink();

    @Override public void contributeMeasurements(Map<String, AGlobalDataPoint> data) {
        for(Map.Entry<String, AtomicInteger> entry: counter.getThreadCounts().entrySet()) {
            final String ident = "Thread Pool " + entry.getKey();
            data.put(ident, new AGlobalDataPoint(ident, entry.getValue().get(), 0));
        }
    }

    @Override public void shutdown() {
    }
}
