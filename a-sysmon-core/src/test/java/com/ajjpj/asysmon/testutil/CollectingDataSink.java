package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.ArrayList;
import java.util.List;

/**
 * @author arno
 */
public class CollectingDataSink implements ADataSink {
    public int numStarted = 0;
    public List<AHierarchicalDataRoot> data = new ArrayList<AHierarchicalDataRoot>();

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
        if("Garbage Collection".equals(identifier)) {
            return;
        }

        numStarted += 1;
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        if("Garbage Collection".equals(data.getRootNode().getIdentifier())) {
            return;
        }

        this.data.add(data);
    }

    @Override public void shutdown() {
    }
}
