package com.ajjpj.asysmon.datasink;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.Collection;

/**
 * @author arno
 */
public interface ADataSink extends AShutdownable {
    void onStartedHierarchicalMeasurement(String identifier);
    void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data);
}
