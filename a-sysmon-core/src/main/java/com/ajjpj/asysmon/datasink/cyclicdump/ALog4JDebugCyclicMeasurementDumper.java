package com.ajjpj.asysmon.datasink.cyclicdump;

import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class ALog4JDebugCyclicMeasurementDumper extends ACyclicMeasurementDumper {
    private static final Logger log = Logger.getLogger(ALog4JDebugCyclicMeasurementDumper.class);

    public ALog4JDebugCyclicMeasurementDumper(int initialDelaySeconds, int frequencyInSeconds, int averagingDelayForScalarsMillis) {
        super(initialDelaySeconds, frequencyInSeconds, averagingDelayForScalarsMillis);
    }

    @Override protected void dump(String s) {
        log.debug(s);
    }
}
