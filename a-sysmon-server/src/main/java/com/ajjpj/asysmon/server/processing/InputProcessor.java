package com.ajjpj.asysmon.server.processing;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;

/**
 * @author arno
 */
public interface InputProcessor {
    void updateSystemClockDiff(InstanceIdentifier instance, long senderTimestamp);

    void addEnvironmentEntry(InstanceIdentifier instance, EnvironmentNode environmentNode);
    void addScalarEntry     (InstanceIdentifier instance, ScalarNode scalarNode);
    void addTraceEntry      (InstanceIdentifier instance, TraceRootNode traceRootNode);
}
