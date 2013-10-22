package com.ajjpj.asysmon;


import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.measure.AMeasurementHierarchy;
import com.ajjpj.asysmon.measure.AMeasurementHierarchyImpl;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ATimer;

import java.util.List;

/**
 * @author arno
 */
public class ASysMon {
    private final ATimer timer;
    private final List<ADataSink> handlers;

    private final ThreadLocal<AMeasurementHierarchy> hierarchyPerThread = new ThreadLocal<AMeasurementHierarchy>();

    public ASysMon(ATimer timer, List<ADataSink> handlers) {
        this.timer = timer;
        this.handlers = handlers; //TODO initialize from props or whatever; the list must be thread safe!
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
        return getMeasurementHierarchy().start(identifier);
    }

    public ASimpleMeasurement start(String identifier, boolean disjoint) {
        return getMeasurementHierarchy().start(identifier, disjoint);
    }


}

