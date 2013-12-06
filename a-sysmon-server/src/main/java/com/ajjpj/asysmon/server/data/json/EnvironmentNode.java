package com.ajjpj.asysmon.server.data.json;


import com.ajjpj.asysmon.server.data.InstanceIdentifier;

/**
 * 'environment' is quasi-static data that describes an application's environment, e.g. software version, OS name and
 *  version, name and version of the application server etc.
 *
 * @author arno
 */
public class EnvironmentNode {
    private InstanceIdentifier instanceIdentifier;
    private String uuid;
    private long senderTimestamp;
    private long adjustedTimestamp;
    private String key;
    private String value;

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

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EnvironmentNode{" +
                "instanceIdentifier=" + instanceIdentifier +
                ", uuid='" + uuid + '\'' +
                ", senderTimestamp=" + senderTimestamp +
                ", adjustedTimestamp=" + adjustedTimestamp +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
