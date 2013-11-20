package com.ajjpj.asysmon.measure.global;


import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.Collection;
import java.util.Map;


public interface AGlobalMeasurer extends AShutdownable {
    void contributeMeasurements(Map<String, AGlobalDataPoint> data);
}
