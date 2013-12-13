package com.ajjpj.asysmon.measure.scalar;


import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.Map;


public interface AScalarMeasurer extends AShutdownable {
    void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp);
}
