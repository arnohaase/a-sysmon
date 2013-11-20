package com.ajjpj.asysmon.datasink.cyclicdump;


import com.ajjpj.asysmon.ASysMon;

/**
 * @author arno
 */
public class AStdOutCyclicMeasurementDumper extends ACyclicMeasurementDumper {
    public AStdOutCyclicMeasurementDumper(ASysMon sysMon, int frequencyInSeconds) {
        super(sysMon, frequencyInSeconds);
    }

    public AStdOutCyclicMeasurementDumper(ASysMon sysMon, int initialDelaySeconds, int frequencyInSeconds) {
        super(sysMon, initialDelaySeconds, frequencyInSeconds);
    }

    @Override protected void dump(String s) {
        System.out.println(s);
    }
}
