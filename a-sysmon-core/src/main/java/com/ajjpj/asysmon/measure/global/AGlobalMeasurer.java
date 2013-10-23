package com.ajjpj.asysmon.measure.global;


import com.ajjpj.asysmon.data.AGlobalDataPoint;

import java.util.Collection;
import java.util.Map;


public interface AGlobalMeasurer {
    void contributeMeasurements(Map<String, AGlobalDataPoint> data);
}
