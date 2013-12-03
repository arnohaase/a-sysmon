package com.ajjpj.asysmon.server.storage;

/**
 * @author arno
 */
public class ScalarDataPoint {
    public final String instance;
    public final long timestamp;
    public final double value;

    public ScalarDataPoint(String instance, long timestamp, double value) {
        this.instance = instance;
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ScalarDataPoint{" +
                "instance='" + instance + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
