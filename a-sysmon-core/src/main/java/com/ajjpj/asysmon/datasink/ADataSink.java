package com.ajjpj.asysmon.datasink;

import com.ajjpj.asysmon.data.AHierarchicalData;

/**
 * @author arno
 */
public interface ADataSink {
    void onStartedHierarchicalMeasurement();
    void onFinishedHierarchicalMeasurement(AHierarchicalData data);
}
