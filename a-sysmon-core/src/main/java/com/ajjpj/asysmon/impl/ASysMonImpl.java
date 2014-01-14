package com.ajjpj.asysmon.impl;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.config.ASysMonAware;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.*;
import com.ajjpj.asysmon.measure.environment.AEnvironmentData;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.AList;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * This class is the point of contact for an application to ASysMon. There are basically two ways to use it:
 *
 * <ul>
 *     <li> Use the static get() method to access it as a singleton. That is simple and convenient, and it is
 *          sufficient for many applications. If it is used that way, all configuration must be done through
 *          the static methods of ADefaultSysMonConfig. </li>
 *     <li> Create and manage your own instance (or instances) by calling the constructor, passing in your
 *          configuration. This is for maximum flexibility, but you lose some convenience. </li>
 * </ul>
 *
 * @author arno
 */
public class ASysMonImpl implements AShutdownable, ASysMonApi {
    private final ASysMonConfig config;
    private volatile AList<RobustDataSinkWrapper> handlers = AList.nil();
    private volatile AList<RobustScalarMeasurerWrapper> scalarMeasurers = AList.nil();
    private volatile AList<RobustEnvironmentMeasurerWrapper> environmentMeasurers = AList.nil();

    private final ThreadLocal<AMeasurementHierarchy> hierarchyPerThread = new ThreadLocal<AMeasurementHierarchy>();

    public ASysMonImpl(ASysMonConfig config) {
        this.config = config;

        for(AScalarMeasurer m: config.initialScalarMeasurers) {
            addScalarMeasurer(m);
        }

        for(ADataSink h: config.initialDataSinks) {
            addDataSink(h);
        }

        for(AEnvironmentMeasurer m: config.initialEnvironmentMeasurers) {
            addEnvironmentMeasurer(m);
        }
    }

    @Override public ASysMonConfig getConfig() {
        return config;
    }

    private void injectSysMon(Object o) {
        if(o instanceof ASysMonAware) {
            ((ASysMonAware) o).setASysMon(this);
        }
    }

    void addScalarMeasurer(AScalarMeasurer m) {
        injectSysMon(m);
        scalarMeasurers = scalarMeasurers.cons(new RobustScalarMeasurerWrapper(m, config.logger, config.measurementTimeoutNanos, config.maxNumMeasurementTimeouts));
    }

    void addEnvironmentMeasurer(AEnvironmentMeasurer m) {
        injectSysMon(m);
        environmentMeasurers = environmentMeasurers.cons(new RobustEnvironmentMeasurerWrapper(m, config.logger, config.measurementTimeoutNanos, config.maxNumMeasurementTimeouts));
    }

    void addDataSink(ADataSink handler) {
        injectSysMon(handler);
        handlers = handlers.cons(new RobustDataSinkWrapper(handler, config.logger, config.dataSinkTimeoutNanos, config.maxNumDataSinkTimeouts));
    }

    private ADataSink getCompositeDataSink() {
        return new ADataSink() {
            @Override public void onStartedHierarchicalMeasurement(String identifier) {
                for(RobustDataSinkWrapper handler: handlers) {
                    handler.onStartedHierarchicalMeasurement(identifier);
                }
            }

            @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
                hierarchyPerThread.remove();

                for(RobustDataSinkWrapper handler: handlers) {
                    handler.onFinishedHierarchicalMeasurement(data);
                }
            }

            @Override public void shutdown() {
            }
        };
    }

    private AMeasurementHierarchy getMeasurementHierarchy() {
        final AMeasurementHierarchy candidate = hierarchyPerThread.get();
        if(candidate != null) {
            return candidate;
        }

        final AMeasurementHierarchy result = new AMeasurementHierarchyImpl(config, getCompositeDataSink());
        hierarchyPerThread.set(result);
        return result;
    }

    @Override public <E extends Exception> void measure(String identifier, AMeasureCallbackVoid<E> callback) throws E {
        final ASimpleMeasurement m = start(identifier);
        try {
            callback.call(m);
        } finally {
            m.finish();
        }
    }

    @Override public <R, E extends Exception> R measure(String identifier, AMeasureCallback<R,E> callback) throws E {
        final ASimpleMeasurement m = start(identifier);
        try {
            return callback.call(m);
        } finally {
            m.finish();
        }
    }

    @Override public ASimpleMeasurement start(String identifier) {
        return start(identifier, true);
    }
    @Override public ASimpleMeasurement start(String identifier, boolean serial) {
        return getMeasurementHierarchy().start(identifier, serial);
    }

    /**
     * This is for the rare case that measurement data was collected by other means and should be 'injected'
     *  into A-SysMon. If you do not understand this, this method is probably not for you.
     */
    @Override public void injectSyntheticMeasurement(AHierarchicalDataRoot d) {
        getCompositeDataSink().onStartedHierarchicalMeasurement(d.getRootNode().getIdentifier());
        getCompositeDataSink().onFinishedHierarchicalMeasurement(d);
    }

    @Override public ACollectingMeasurement startCollectingMeasurement(String identifier) {
        return startCollectingMeasurement(identifier, true);
    }
    @Override public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean serial) {
        return getMeasurementHierarchy().startCollectingMeasurement(identifier, serial);
    }

    @Override public Map<String, AScalarDataPoint> getScalarMeasurements() {
        return getScalarMeasurements(config.averagingDelayForScalarsMillis);
    }

    @Override public Map<String, AScalarDataPoint> getScalarMeasurements(int averagingDelayForScalarsMillis) {
        final Map<String, AScalarDataPoint> result = new TreeMap<String, AScalarDataPoint>();
        if(config.isGloballyDisabled()) {
            return result;
        }

        final Map<String, Object> mementos = new TreeMap<String, Object>();
        for(RobustScalarMeasurerWrapper measurer: scalarMeasurers) {
            measurer.prepareMeasurements(mementos);
        }

        try {
            Thread.sleep(averagingDelayForScalarsMillis);
        } catch (InterruptedException e) { //
        }

        final long now = System.currentTimeMillis();
        for(RobustScalarMeasurerWrapper measurer: scalarMeasurers) {
            measurer.contributeMeasurements(result, now, mementos);
        }
        return result;
    }

    @Override public Map<AList<String>, AEnvironmentData> getEnvironmentMeasurements() {
        final Map<AList<String>, AEnvironmentData> result = new HashMap<AList<String>, AEnvironmentData>();
        if(config.isGloballyDisabled()) {
            return result;
        }

        for(RobustEnvironmentMeasurerWrapper m: environmentMeasurers) {
            m.contributeMeasurements(new AEnvironmentMeasurer.EnvironmentCollector(result));
        }
        return result;
    }

    @Override public void shutdown() {
        config.logger.info("shutting down A-SysMon");

        for(RobustDataSinkWrapper handler: handlers) {
            handler.shutdown();
        }
        for (RobustScalarMeasurerWrapper m: scalarMeasurers) {
            m.shutdown();
        }

        for(RobustEnvironmentMeasurerWrapper m: environmentMeasurers) {
            m.shutdown();
        }

        config.logger.info("finished shutting down A-SysMon");
    }
}

