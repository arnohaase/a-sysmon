package com.ajjpj.asysmon.datasink.cyclicdump;

/**
 * @author arno
 */
public class AStdOutCyclicMeasurementDumper extends ACyclicMeasurementDumper {
    public AStdOutCyclicMeasurementDumper(int initialDelaySeconds, int frequencyInSeconds, int averagingDelayMillis) {
        super(initialDelaySeconds, frequencyInSeconds, averagingDelayMillis);
    }

    @Override protected void dump(String s) {
        System.out.println(s);
    }
}
