package com.ajjpj.asysmon.datasink.offloadhttpjson;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.Collection;


/**
 * @author arno
 */
public class AHttpJsonOffloadingDataSink implements ADataSink {
    @Override public void onStartedHierarchicalMeasurement() {
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
    }

    @Override public void shutdown() {
        //TODO
    }
}
