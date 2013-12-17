package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

/**
 * @author arno
 */
public class CountingDataSink implements ADataSink {
    public int started = 0;
    public int finished = 0;

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
        started += 1;
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        finished += 1;
    }

    @Override public void shutdown() throws Exception {
    }
}
