package com.ajjpj.asysmon.datasink.aggregation.minmaxavg;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.datasink.aggregation.AMinMaxAvgData;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class AMinMaxAvgDataSink implements ADataSink {
    private volatile boolean isActive = false;

    private final ConcurrentHashMap<String, AMinMaxAvgData> rootMap = new ConcurrentHashMap<String, AMinMaxAvgData>();

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public synchronized void clear() {
        rootMap.clear();
    }

    public Map<String, AMinMaxAvgData> getData() {
        return Collections.unmodifiableMap(rootMap);
    }

    @Override public void onStartedHierarchicalMeasurement() {
    }

    @Override
    public void onFinishedHierarchicalMeasurement(AHierarchicalData data, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
        if(isActive) {
            synchronizedCollect(data);
        }
    }

    private synchronized void synchronizedCollect(AHierarchicalData newData) { //TODO change this to a non-blocking implementation
        recCollect(newData, rootMap);
    }

    private void recCollect(AHierarchicalData data, ConcurrentHashMap<String, AMinMaxAvgData> parentMap) {
        final AMinMaxAvgData prev = parentMap.get(data.getIdentifier());

        final ConcurrentHashMap<String, AMinMaxAvgData> childMap;

        if(prev == null) {
            final AMinMaxAvgData newData = new AMinMaxAvgData(data.isSerial(), data.getDurationNanos());
            childMap = newData.getChildren();
            parentMap.put(data.getIdentifier(), newData);
        }
        else {
            childMap = prev.getChildren();
            parentMap.put(data.getIdentifier(), prev.withDataPoint(data.isSerial(), data.getDurationNanos()));
        }

        for(AHierarchicalData childData: data.getChildren()) {
            recCollect(childData, childMap);
        }
    }
}
