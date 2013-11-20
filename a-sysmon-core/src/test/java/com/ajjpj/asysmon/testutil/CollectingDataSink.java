package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author arno
 */
public class CollectingDataSink implements ADataSink {
    public int numStarted = 0;
    public List<AHierarchicalData> data = new ArrayList<AHierarchicalData>();
    public List<Collection<ACorrelationId>> startedFlows = new ArrayList<Collection<ACorrelationId>>();
    public List<Collection<ACorrelationId>> joinedFlows = new ArrayList<Collection<ACorrelationId>>();

    @Override public void onStartedHierarchicalMeasurement() {
        numStarted += 1;
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
        this.data.add(data);
        this.startedFlows.add(startedFlows);
        this.joinedFlows.add(joinedFlows);
    }

    @Override public void shutdown() {
    }
}
