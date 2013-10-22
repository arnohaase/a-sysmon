package com.ajjpj.asysmon.measure;


/**
 * @author arno
 */
public interface AMeasurementHierarchy {
    ASimpleMeasurement start(String identifier);
    ASimpleMeasurement start(String identifier, boolean disjoint);

    void finish(ASimpleMeasurement measurement);
}
