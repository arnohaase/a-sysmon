package com.ajjpj.asysmon.server.processing;


import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import com.ajjpj.asysmon.util.ASoftlyLimitedQueue;
import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class InputProcessorImpl implements InputProcessor {
    private static final Logger log = Logger.getLogger(InputProcessorImpl.class);

    private final SystemClockCorrector systemClockCorrector;

    private final ASoftlyLimitedQueue<EnvironmentNode> environmentQueue;
    private final ASoftlyLimitedQueue<ScalarNode> scalarQueue;
    private final ASoftlyLimitedQueue<TraceRootNode> traceQueue;

    public InputProcessorImpl(SystemClockCorrector systemClockCorrector, ASysMonServerConfig config) {
        this.systemClockCorrector = systemClockCorrector;
        this.environmentQueue = new ASoftlyLimitedQueue<>(config.getEnvironmentQueueSize(), new Log4JWarnCallback("environment queue overflow, discarding old entry"));
        this.scalarQueue      = new ASoftlyLimitedQueue<>(config.getScalarQueueSize(),      new Log4JWarnCallback("scalar queue overflow, discarding old entry"));
        this.traceQueue       = new ASoftlyLimitedQueue<>(config.getTraceQueueSize(),       new Log4JWarnCallback("trace queue overflow, discarding old entry"));

        //TODO start processing workers
    }

    @Override public void updateSystemClockDiff(InstanceIdentifier instance, long senderTimestamp) {
        systemClockCorrector.updateSystemClockDiff(instance, senderTimestamp);
    }

    @Override public void addEnvironmentEntry(InstanceIdentifier instance, EnvironmentNode environmentNode) {
        final long adjustedTimestamp = systemClockCorrector.correctedTimestamp(instance, environmentNode.getSenderTimestamp());
        environmentNode.setAdjustedTimestamp(adjustedTimestamp);
        environmentQueue.add(environmentNode);
    }

    @Override public void addScalarEntry(InstanceIdentifier instance, ScalarNode scalarNode) {
        final long adjustedTimestamp = systemClockCorrector.correctedTimestamp(instance, scalarNode.getSenderTimestamp());
        scalarNode.setAdjustedTimestamp(adjustedTimestamp);
        scalarQueue.add(scalarNode);
    }

    @Override public void addTraceEntry(InstanceIdentifier instance, TraceRootNode traceRootNode) {
        adjustTimestampRec(instance, traceRootNode.getTrace());
        traceQueue.add(traceRootNode);
    }

    private void adjustTimestampRec(InstanceIdentifier instance, TraceNode traceNode) {
        final long adjustedTimestamp = systemClockCorrector.correctedTimestamp(instance, traceNode.getSenderStartTimeMillis());
        traceNode.setAdjustedStartTimeMillis(adjustedTimestamp);

        for(TraceNode child: traceNode.getChildren()) {
            adjustTimestampRec(instance, child);
        }
    }

    private static class Log4JWarnCallback implements Runnable {
        private final String msg;

        private Log4JWarnCallback(String msg) {
            this.msg = msg;
        }

        @Override public void run() {
            log.warn(msg);
        }
    }
}
