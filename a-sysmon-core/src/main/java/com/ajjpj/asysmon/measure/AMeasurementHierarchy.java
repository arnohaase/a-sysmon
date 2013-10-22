package com.ajjpj.asysmon.measure;


/**
 * @author arno
 */
public interface AMeasurementHierarchy {
    ASimpleMeasurement start(String identifier, boolean disjoint);
    void finish(ASimpleMeasurement measurement);

    ACollectingMeasurement startCollectingMeasurement(String identifier, boolean disjoint);
    void finish(ACollectingMeasurement measurement);
}
