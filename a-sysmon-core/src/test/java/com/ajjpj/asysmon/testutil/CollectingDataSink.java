package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author arno
 */
public class CollectingDataSink implements ADataSink {
    public int numStarted = 0;
    public List<AHierarchicalDataRoot> data = new ArrayList<AHierarchicalDataRoot>();

    @Override public void onStartedHierarchicalMeasurement() {
        numStarted += 1;
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        this.data.add(data);
    }

    @Override public void shutdown() {
    }
}
