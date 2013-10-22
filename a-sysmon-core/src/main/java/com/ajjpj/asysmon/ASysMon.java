package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.measure.AMeasurementHierarchy;
import com.ajjpj.asysmon.measure.AMeasurementHierarchyImpl;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ATimer;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is the point of contact for an application to ASysMon. There are basically two ways to use it:
 *
 * <ul>
 *     <li> Use the static get() method to access it as a singleton. That is simple and convenient, and it is
 *          sufficient for many applications. If it is used that way, all configuration must be done through
 *          the static methods of AStaticSysMonConfig. </li>
 *     <li> Create and manage your own instance (or instances) by calling the constructor, passing in your
 *          configuration. This is for maximum flexibility, but you lose some convenience. </li>
 * </ul>
 *
 * @author arno
 */
public class ASysMon {
    private final ATimer timer;
    private final List<ADataSink> handlers;

    private final ThreadLocal<AMeasurementHierarchy> hierarchyPerThread = new ThreadLocal<AMeasurementHierarchy>();

    public static ASysMon get() {
        // this class has the sole purpose of providing really lazy init of the singleton instance
        return ASysMonInstanceHolder.INSTANCE;
    }

    public ASysMon(ASysMonConfig config) {
        this.timer = config.getTimer();
        this.handlers = new ArrayList<ADataSink>(config.getHandlers());
    }

    private AMeasurementHierarchy getMeasurementHierarchy() {
        final AMeasurementHierarchy candidate = hierarchyPerThread.get();
        if(candidate != null) {
            return candidate;
        }

        final AMeasurementHierarchy result = new AMeasurementHierarchyImpl(timer, new ADataSink() {
            @Override public void onFinishedHierarchicalData(AHierarchicalData data) {
                hierarchyPerThread.remove();

                for(ADataSink handler: handlers) {
                    handler.onFinishedHierarchicalData(data);
                }
            }
        });
        hierarchyPerThread.set(result);
        return result;
    }

    public ASimpleMeasurement start(String identifier) {
        return start(identifier, true);
    }

    public ASimpleMeasurement start(String identifier, boolean disjoint) {
        return getMeasurementHierarchy().start(identifier, disjoint);
    }

    /**
     * this class has the sole purpose of providing really lazy init of the singleton instance
     */
    private static class ASysMonInstanceHolder {
        public static final ASysMon INSTANCE = new ASysMon(AStaticSysMonConfig.get());
    }
}

