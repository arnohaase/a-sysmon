package com.ajjpj.asysmon.measure.threadpool;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.impl.ASysMonConfigurer;
import com.ajjpj.asysmon.config.ASysMonAware;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public class AThreadCountMeasurer implements AScalarMeasurer, ASysMonAware {
    private final AThreadPoolTrackingDataSink counter = new AThreadPoolTrackingDataSink();

    @Override public void setASysMon(ASysMonApi sysMon) {
        ASysMonConfigurer.addDataSink(sysMon, counter);
    }

    @Override public void prepareMeasurements(Map<String, Object> mementos) {
    }

    @Override
    public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
        for(Map.Entry<String, AtomicInteger> entry: counter.getThreadCounts().entrySet()) {
            final String ident = "Thread Pool " + entry.getKey();
            data.put(ident, new AScalarDataPoint(timestamp, ident, entry.getValue().get(), 0));
        }
    }

    @Override public void shutdown() {
    }
}
