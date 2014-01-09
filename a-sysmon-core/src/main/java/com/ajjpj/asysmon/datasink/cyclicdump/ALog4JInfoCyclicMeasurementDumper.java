package com.ajjpj.asysmon.datasink.cyclicdump;

import com.ajjpj.asysmon.ASysMon;
import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class ALog4JInfoCyclicMeasurementDumper extends ACyclicMeasurementDumper {
    private static final Logger log = Logger.getLogger(ALog4JInfoCyclicMeasurementDumper.class);

    public ALog4JInfoCyclicMeasurementDumper(ASysMon sysMon, int frequencyInSeconds) {
        super(sysMon, frequencyInSeconds);
    }

    public ALog4JInfoCyclicMeasurementDumper(ASysMon sysMon, int initialDelaySeconds, int frequencyInSeconds, int averagingDelayForScalarsMillis) {
        super(sysMon, initialDelaySeconds, frequencyInSeconds, averagingDelayForScalarsMillis);
    }

    @Override protected void dump(String s) {
        log.info(s);
    }
}
