package com.ajjpj.asysmon.datasink.cyclicdump;

import com.ajjpj.asysmon.ASysMon;
import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class ALog4JDebugCyclicMeasurementDumper extends ACyclicMeasurementDumper {
    private static final Logger log = Logger.getLogger(ALog4JDebugCyclicMeasurementDumper.class);

    public ALog4JDebugCyclicMeasurementDumper(ASysMon sysMon, int frequencyInSeconds) {
        super(sysMon, frequencyInSeconds);
    }

    public ALog4JDebugCyclicMeasurementDumper(ASysMon sysMon, int initialDelaySeconds, int frequencyInSeconds) {
        super(sysMon, initialDelaySeconds, frequencyInSeconds);
    }

    @Override protected void dump(String s) {
        log.debug(s);
    }
}
