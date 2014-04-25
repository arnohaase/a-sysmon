package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.ACollectingMeasurement;
import com.ajjpj.asysmon.measure.AMeasureCallback;
import com.ajjpj.asysmon.measure.AMeasureCallbackVoid;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.measure.environment.AEnvironmentData;

import java.util.List;
import java.util.Map;


/**
 * @author arno
 */
public interface ASysMonApi {
    ASysMonConfig getConfig();

    <E extends Exception> void measure(String identifier, AMeasureCallbackVoid<E> callback) throws E;
    <R, E extends Exception> R measure(String identifier, AMeasureCallback<R,E> callback) throws E;

    /**
     * This tells A-SysMon that the currently running measurement starts a new 'flow'. Other measurements (in this
     *  or in another JVM) may 'join' that flow, so that these measurements can be evaluated at a later time. Typical
     *  examples of this is processing done in a spawned thread, batch processing that uses several worker threads,
     *  or asynchronous web service calls.<p />
     *
     * It is an invalid to call this method without a surrounding measurement, and doing so throws an exception.
     */
    void startFlow(ACorrelationId flowId);

    void joinFlow(ACorrelationId flowId);

    ASimpleMeasurement start(String identifier);
    ASimpleMeasurement start(String identifier, boolean serial);

    /**
     * returns true iff a simple measurement is currently running for this thread
     */
    boolean hasRunningMeasurement();

    /**
     * This is for the rare case that measurement data was collected by other means and should be 'injected'
     *  into A-SysMon. If you do not understand this, this method is probably not for you.
     */
    void injectSyntheticMeasurement(AHierarchicalDataRoot d);

    public ACollectingMeasurement startCollectingMeasurement(String identifier);
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean serial);

    public Map<String, AScalarDataPoint> getScalarMeasurements();

    Map<String, AScalarDataPoint> getScalarMeasurements(int averagingDelayForScalarsMillis);

    List<AEnvironmentData> getEnvironmentMeasurements() throws Exception;
}
