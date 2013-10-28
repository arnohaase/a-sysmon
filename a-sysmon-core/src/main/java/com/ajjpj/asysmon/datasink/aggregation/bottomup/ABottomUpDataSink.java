package com.ajjpj.asysmon.datasink.aggregation.bottomup;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.datasink.aggregation.AMinMaxAvgData;
import com.ajjpj.asysmon.util.ArrayStack;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arno
 */
public class ABottomUpDataSink implements ADataSink {
    private volatile boolean isActive = false;

    /**
     * Leaf node - e.g. JDBC call - at the top, service calls 'below' that, with the entry points as leaves. Statistics are collected
     *  per context, i.e. per parent in this tree.
     */
    private final ConcurrentHashMap<String, AMinMaxAvgData> rootMap = new ConcurrentHashMap<String, AMinMaxAvgData>();

    private final ABottomUpLeafFilter leafFilter;

    public ABottomUpDataSink(ABottomUpLeafFilter leafFilter) {
        this.leafFilter = leafFilter;
    }

    public synchronized void clear() {
        rootMap.clear();
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public boolean isActive() {
        return this.isActive;
    }

    @Override public void onStartedHierarchicalMeasurement() {
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data) {
        if(isActive) {
            synchronizedCollect(data);
        }
    }

    public Map<String, AMinMaxAvgData> getData() {
        return Collections.unmodifiableMap(rootMap);
    }

    private synchronized void synchronizedCollect(AHierarchicalData newData) { //TODO change this to a non-blocking implementation
        recCollect(newData, new ArrayStack<AHierarchicalData>());
    }

    private void recCollect(AHierarchicalData newData, ArrayStack<AHierarchicalData> callStack) {
        callStack.push(newData);

        if(leafFilter.isLeaf(newData)) {
            doStoreJdbc(newData, callStack, rootMap);
        }
        else {
            for(AHierarchicalData child: newData.getChildren()) {
                recCollect(child, callStack);
            }
        }
        callStack.pop();
    }

    private void doStoreJdbc(AHierarchicalData jdbcData, Iterable<AHierarchicalData> callStack, Map<String, AMinMaxAvgData> parentMap) {
        for(AHierarchicalData data: callStack) {
            final AMinMaxAvgData aggregated = registerData(data.getIdentifier(), parentMap, data.isSerial(), data.getDurationNanos());
            parentMap = aggregated.getChildren();
        }
    }

    private AMinMaxAvgData registerData(String key, Map<String, AMinMaxAvgData> map, boolean isSerial, long durationNanos) {
        AMinMaxAvgData result = map.get(key);
        if(result == null) {
            result = new AMinMaxAvgData(isSerial, durationNanos);
        }
        else {
            result = result.withDataPoint(isSerial, durationNanos);
        }
        map.put(key, result);
        return result;
    }
}
