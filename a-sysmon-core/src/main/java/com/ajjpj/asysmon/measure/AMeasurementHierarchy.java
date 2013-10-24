package com.ajjpj.asysmon.measure;


/**
 * @author arno
 */
public interface AMeasurementHierarchy {
    ASimpleMeasurement start(String identifier, boolean isSerial);
    void finish(ASimpleSerialMeasurementImpl measurement);
    void finish(ASimpleParallelMeasurementImpl measurement);

    ACollectingMeasurement startCollectingMeasurement(String identifier, boolean isSerial);
    void finish(ACollectingMeasurement measurement);
}
