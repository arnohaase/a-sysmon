package com.ajjpj.asysmon.server.data.json;

import java.util.ArrayList;
import java.util.List;

/**
 * @author arno
 */
public class RootNode {
    private String sender;
    private String senderInstance;
    private long senderTimestamp;
    private long adjustedTimestamp;

    private List<TraceRootNode> traces = new ArrayList<>();
    private List<ScalarNode> scalars = new ArrayList<>();
    private List<EnvironmentNode> environment = new ArrayList<>();


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderInstance() {
        return senderInstance;
    }

    public void setSenderInstance(String senderInstance) {
        this.senderInstance = senderInstance;
    }

    public long getSenderTimestamp() {
        return senderTimestamp;
    }

    public long getAdjustedTimestamp() {
        return adjustedTimestamp;
    }

    public void setAdjustedTimestamp(long adjustedTimestamp) {
        this.adjustedTimestamp = adjustedTimestamp;
    }

    public void setSenderTimestamp(long senderTimestamp) {
        this.senderTimestamp = senderTimestamp;
    }

    public List<TraceRootNode> getTraces() {
        return traces;
    }

    public void setTraces(List<TraceRootNode> traces) {
        this.traces = traces;
    }

    public List<ScalarNode> getScalars() {
        return scalars;
    }

    public void setScalars(List<ScalarNode> scalars) {
        this.scalars = scalars;
    }

    public List<EnvironmentNode> getEnvironment() {
        return environment;
    }

    public void setEnvironment(List<EnvironmentNode> environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "RootNode{" +
                "sender='" + sender + '\'' +
                ", senderInstance='" + senderInstance + '\'' +
                ", senderTimestamp=" + senderTimestamp +
                ", adjustedTimestamp=" + adjustedTimestamp +
                ", traces=" + traces +
                ", scalars=" + scalars +
                ", environment=" + environment +
                '}';
    }
}
