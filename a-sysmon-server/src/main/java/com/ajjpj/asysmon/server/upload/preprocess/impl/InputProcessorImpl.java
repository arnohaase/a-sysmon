package com.ajjpj.asysmon.server.upload.preprocess.impl;


import com.ajjpj.asysmon.server.upload.preprocess.InputProcessor;
import com.ajjpj.asysmon.server.upload.preprocess.SystemClockCorrector;
import com.ajjpj.asysmon.server.eventbus.EventBus;
import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import org.apache.log4j.Logger;

import javax.inject.Inject;

/**
 * @author arno
 */
public class InputProcessorImpl implements InputProcessor {
    private static final Logger log = Logger.getLogger(InputProcessorImpl.class);

    private final SystemClockCorrector systemClockCorrector;
    private final EventBus eventBus;

    @Inject
    public InputProcessorImpl(SystemClockCorrector systemClockCorrector, EventBus eventBus) {
        this.systemClockCorrector = systemClockCorrector;
        this.eventBus = eventBus;
    }

    @Override public void updateSystemClockDiff(InstanceIdentifier instance, long senderTimestamp) {
        systemClockCorrector.updateSystemClockDiff(instance, senderTimestamp);
    }

    @Override public void addEnvironmentEntry(InstanceIdentifier instance, EnvironmentNode environmentNode) {
        environmentNode.setInstanceIdentifier(instance);
        final long adjustedTimestamp = systemClockCorrector.correctedTimestamp(instance, environmentNode.getSenderTimestamp());
        environmentNode.setAdjustedTimestamp(adjustedTimestamp);
        eventBus.fireNewEnvironmentData(environmentNode);
    }

    @Override public void addScalarEntry(InstanceIdentifier instance, ScalarNode scalarNode) {
        scalarNode.setInstanceIdentifier(instance);
        final long adjustedTimestamp = systemClockCorrector.correctedTimestamp(instance, scalarNode.getSenderTimestamp());
        scalarNode.setAdjustedTimestamp(adjustedTimestamp);
        eventBus.fireNewScalarData(scalarNode);
    }

    @Override public void addTraceEntry(InstanceIdentifier instance, TraceRootNode traceRootNode) {
        traceRootNode.setInstanceIdentifier(instance);
        adjustTimestampRec(instance, traceRootNode.getTrace());
        eventBus.fireNewTrace(traceRootNode);
    }

    private void adjustTimestampRec(InstanceIdentifier instance, TraceNode traceNode) {
        final long adjustedTimestamp = systemClockCorrector.correctedTimestamp(instance, traceNode.getSenderStartTimeMillis());
        traceNode.setAdjustedStartTimeMillis(adjustedTimestamp);

        for(TraceNode child: traceNode.getChildren()) {
            adjustTimestampRec(instance, child);
        }
    }
}
