package com.ajjpj.asysmon.measure;


/**
 * @author arno
 */
public interface AMeasurementHierarchy {
    ASimpleMeasurement start(String identifier, boolean disjoint);
    void finish(ASimpleSerialMeasurementImpl measurement);
    void finish(ASimpleParallelMeasurementImpl measurement);

    ACollectingMeasurement startCollectingMeasurement(String identifier, boolean disjoint);
    void finish(ACollectingMeasurement measurement);
}
