package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.AGlobalConfig;
import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.*;
import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;
import com.ajjpj.asysmon.util.AList;
import com.ajjpj.asysmon.util.AShutdownable;
import com.ajjpj.asysmon.util.timer.ATimer;

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
public class ASysMon implements AShutdownable {
    private final ATimer timer;
    private volatile AList<ADataSink> handlers = AList.nil();
    private volatile AList<AGlobalMeasurer> globalMeasurers = AList.nil();

    private final ThreadLocal<AMeasurementHierarchy> hierarchyPerThread = new ThreadLocal<AMeasurementHierarchy>();

    public static ASysMon get() {
        // this class has the sole purpose of providing really lazy init of the singleton instance
        return ASysMonInstanceHolder.INSTANCE;
    }

    public ASysMon() {
        this(AGlobalConfig.getTimer());
    }

    public ASysMon(ATimer timer) {
        this.timer = timer;
        for(AGlobalMeasurer m: AGlobalConfig.getGlobalMeasurers()) {
            globalMeasurers = globalMeasurers.cons(m);
        }
    }

    public ASysMon(ADataSink... handlers) {
        this(AGlobalConfig.getTimer(), handlers);
    }

    public ASysMon(ATimer timer, ADataSink... handlers) {
        this.timer = timer;
        for(ADataSink h: handlers) {
            this.handlers = this.handlers.cons(h);
        }
    }

    synchronized void addGlobalMeasurer(AGlobalMeasurer m) {
        globalMeasurers = globalMeasurers.cons(m);
    }

    synchronized void addDataSink(ADataSink handler) {
        handlers = handlers.cons(handler);
    }

    private AMeasurementHierarchy getMeasurementHierarchy() {
        final AMeasurementHierarchy candidate = hierarchyPerThread.get();
        if(candidate != null) {
            return candidate;
        }

        final AMeasurementHierarchy result = new AMeasurementHierarchyImpl(timer, new ADataSink() {
            @Override public void onStartedHierarchicalMeasurement() {
                for(ADataSink handler: handlers) {
                    handler.onStartedHierarchicalMeasurement();
                }
            }

            @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
                hierarchyPerThread.remove();

                for(ADataSink handler: handlers) {
                    handler.onFinishedHierarchicalMeasurement(data);
                }
            }

            @Override public void shutdown() {
            }
        });
        hierarchyPerThread.set(result);
        return result;
    }

    public <E extends Exception> void measure(String identifier, AMeasureCallbackVoid<E> callback) throws E {
        final ASimpleMeasurement m = start(identifier);
        try {
            callback.call(m);
        } finally {
            m.finish();
        }
    }

    public <R, E extends Exception> R measure(String identifier, AMeasureCallback<R,E> callback) throws E {
        final ASimpleMeasurement m = start(identifier);
        try {
            return callback.call(m);
        } finally {
            m.finish();
        }
    }

    public ASimpleMeasurement start(String identifier) {
        return start(identifier, true);
    }
    public ASimpleMeasurement start(String identifier, boolean serial) {
        return getMeasurementHierarchy().start(identifier, serial);
    }

    public ACollectingMeasurement startCollectingMeasurement(String identifier) {
        return startCollectingMeasurement(identifier, true);
    }
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean serial) {
        return getMeasurementHierarchy().startCollectingMeasurement(identifier, serial);
    }

    public Map<String, AGlobalDataPoint> getGlobalMeasurements() {
        if(AGlobalConfig.isGloballyDisabled()) {
            return new HashMap<String, AGlobalDataPoint>();
        }
        final Map<String, AGlobalDataPoint> result = new TreeMap<String, AGlobalDataPoint>();
        for(AGlobalMeasurer measurer: globalMeasurers) {
            measurer.contributeMeasurements(result);
        }
        return result;
    }

    @Override public void shutdown() {
        //TODO log

        for(ADataSink handler: handlers) {
            try {
                handler.shutdown();
            } catch (Exception e) {
                e.printStackTrace(); //TODO log
            }
        }
        for(AGlobalMeasurer m: globalMeasurers) {
            try {
                m.shutdown();
            } catch (Exception e) {
                e.printStackTrace(); //TODO log
            }
        }

        //TODO log
    }

    /**
     * this class has the sole purpose of providing really lazy init of the singleton instance
     */
    private static class ASysMonInstanceHolder {
        public static final ASysMon INSTANCE = new ASysMon();
    }
}

