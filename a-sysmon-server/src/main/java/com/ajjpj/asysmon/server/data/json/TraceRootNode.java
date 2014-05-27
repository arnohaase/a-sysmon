package com.ajjpj.asysmon.server.data.json;


import com.ajjpj.asysmon.server.data.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;


/**
 * 'Trace' data refers to execution traces, i.e. a hierarchy of measurements performed in an application. This is the
 *  root node of such a trace, containing the actual trace as well as lists of started and joined flows.<p>
 *
 * A 'flow' is basically an ID that links several traces to each other. This is typically used for asynchronous
 *  execution or messaging. The identifiers are often called 'correlation IDs'.
 *
 * @author arno
 */
public class TraceRootNode {
    private InstanceIdentifier instanceIdentifier;
    private String uuid;

    private TraceNode trace;

    private List<CorrelationId> startedFlows = new ArrayList<>();
    private List<CorrelationId> joinedFlows = new ArrayList<>();

    public InstanceIdentifier getInstanceIdentifier() {
        return instanceIdentifier;
    }
    public void setInstanceIdentifier(InstanceIdentifier instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public TraceNode getTrace() {
        return trace;
    }
    public void setTrace(TraceNode trace) {
        this.trace = trace;
    }

    public List<CorrelationId> getStartedFlows() {
        return startedFlows;
    }
    public void setStartedFlows(List<CorrelationId> startedFlows) {
        this.startedFlows = startedFlows;
    }

    public List<CorrelationId> getJoinedFlows() {
        return joinedFlows;
    }
    public void setJoinedFlows(List<CorrelationId> joinedFlows) {
        this.joinedFlows = joinedFlows;
    }

    @Override
    public String toString() {
        return "TraceRootNode{" +
                "instanceIdentifier=" + instanceIdentifier +
                ", uuid='" + uuid + '\'' +
                ", trace=" + trace +
                ", startedFlows=" + startedFlows +
                ", joinedFlows=" + joinedFlows +
                '}';
    }
}
