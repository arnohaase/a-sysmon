package com.ajjpj.asysmon.processing;

import com.ajjpj.asysmon.data.AHierarchicalData;

/**
 * @author arno
 */
public interface ADataSink {
    void onStartHierarchicalMeasurement();
    void onFinishedHierarchicalMeasurement(AHierarchicalData data);
}
