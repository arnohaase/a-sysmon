package com.ajjpj.asysmon.measure;


import com.ajjpj.asysmon.data.ACorrelationId;

/**
 * @author arno
 */
public interface AMeasurementHierarchy {
    String IDENT_SYNTHETIC_ROOT = "<synthetic>";

    ASimpleMeasurement start(String identifier, boolean isSerial);
    void finish(ASimpleSerialMeasurementImpl measurement);
    void finish(ASimpleParallelMeasurementImpl measurement);

    ACollectingMeasurement startCollectingMeasurement(String identifier, boolean isSerial);
    void finish(ACollectingMeasurement measurement);

    void onStartFlow(ACorrelationId flowId);
    void onJoinFlow(ACorrelationId flowId);
}
