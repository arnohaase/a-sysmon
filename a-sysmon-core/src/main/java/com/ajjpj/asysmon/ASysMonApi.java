package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.ACollectingMeasurement;
import com.ajjpj.asysmon.measure.AMeasureCallback;
import com.ajjpj.asysmon.measure.AMeasureCallbackVoid;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.measure.environment.AEnvironmentData;
import com.ajjpj.asysmon.util.AList;

import java.util.Map;


/**
 * @author arno
 */
public interface ASysMonApi {
    ASysMonConfig getConfig();

    <E extends Exception> void measure(String identifier, AMeasureCallbackVoid<E> callback) throws E;
    <R, E extends Exception> R measure(String identifier, AMeasureCallback<R,E> callback) throws E;

    ASimpleMeasurement start(String identifier);
    ASimpleMeasurement start(String identifier, boolean serial);

    /**
     * This is for the rare case that measurement data was collected by other means and should be 'injected'
     *  into A-SysMon. If you do not understand this, this method is probably not for you.
     */
    void injectSyntheticMeasurement(AHierarchicalDataRoot d);

    public ACollectingMeasurement startCollectingMeasurement(String identifier);
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean serial);

    public Map<String, AScalarDataPoint> getScalarMeasurements();

    Map<String, AScalarDataPoint> getScalarMeasurements(int averagingDelayForScalarsMillis);

    Map<AList<String>, AEnvironmentData> getEnvironmentMeasurements() throws Exception;
}
