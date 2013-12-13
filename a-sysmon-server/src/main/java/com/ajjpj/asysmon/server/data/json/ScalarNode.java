package com.ajjpj.asysmon.server.data.json;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;

/**
 * A 'scalar measurement' or 'scalar' for short is a numeric measurement of some quantity in an application. This includes
 *  a wide range of kinds of numbers, e.g. system and CPU load, current heap size, or the number of threaddump or database
 *  connections currently in use. <p />
 *
 * In order for such data to be presented in a non-generic, meaningful way, scalar measurement types must be
 *  registered in the server. TODO how?
 *
 * @author arno
 */
public class ScalarNode {
    private InstanceIdentifier instanceIdentifier;
    private String uuid;
    private long senderTimestamp;
    private long adjustedTimestamp;
    private String name;
    private double value;

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

    public long getSenderTimestamp() {
        return senderTimestamp;
    }
    public void setSenderTimestamp(long senderTimestamp) {
        this.senderTimestamp = senderTimestamp;
    }

    public long getAdjustedTimestamp() {
        return adjustedTimestamp;
    }
    public void setAdjustedTimestamp(long adjustedTimestamp) {
        this.adjustedTimestamp = adjustedTimestamp;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ScalarNode{" +
                "instanceIdentifier=" + instanceIdentifier +
                ", uuid='" + uuid + '\'' +
                ", senderTimestamp=" + senderTimestamp +
                ", adjustedTimestamp=" + adjustedTimestamp +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
