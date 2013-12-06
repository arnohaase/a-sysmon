package com.ajjpj.asysmon.server.data.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author arno
 */
public class TraceNode {
    private boolean isSerial;

    private long senderStartTimeMillis;
    private long adjustedStartTimeMillis;
    private long durationNanos;
    private String identifier;

    private Map<String, String> parameters = new HashMap<>();
    private List<TraceNode> children = new ArrayList<>();

    public boolean getIsSerial() {
        return isSerial;
    }
    public void setIsSerial(boolean isSerial) {
        this.isSerial = isSerial;
    }

    public long getSenderStartTimeMillis() {
        return senderStartTimeMillis;
    }
    public void setSenderStartTimeMillis(long senderStartTimeMillis) {
        this.senderStartTimeMillis = senderStartTimeMillis;
    }

    public long getAdjustedStartTimeMillis() {
        return adjustedStartTimeMillis;
    }

    public void setAdjustedStartTimeMillis(long adjustedStartTimeMillis) {
        this.adjustedStartTimeMillis = adjustedStartTimeMillis;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public void setDurationNanos(long durationNanos) {
        this.durationNanos = durationNanos;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public List<TraceNode> getChildren() {
        return children;
    }

    public void setChildren(List<TraceNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "TraceNode{" +
                "isSerial=" + isSerial +
                ", senderStartTimeMillis=" + senderStartTimeMillis +
                ", adjustedStartTimeMillis=" + adjustedStartTimeMillis +
                ", durationNanos=" + durationNanos +
                ", identifier='" + identifier + '\'' +
                ", parameters=" + parameters +
                ", children=" + children +
                '}';
    }
}
