package com.ajjpj.asysmon.datasink;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;

import java.util.Collection;

/**
 * @author arno
 */
public interface ADataSink {
    void onStartedHierarchicalMeasurement();
    void onFinishedHierarchicalMeasurement(AHierarchicalData data, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows);
}
