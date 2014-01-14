package com.ajjpj.asysmon;


import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.measure.threadpool.AThreadCountMeasurer;

/**
 * This class can change the configuration of an existing ASysMon instance. This is done to avoid race conditions
 *  during application startup, e.g. if ASysMon is used during Spring startup, and servlets need to contribute
 *  configuration later.<p/>
 *
 * These methods are not part of ASysMon itself to keep that API lean and clean: These methods are for use during system
 *  initialization.
 *
 * @author arno
 */
public class ASysMonConfigurer {
    public static void addScalarMeasurer(ASysMon sysMon, AScalarMeasurer m) {
        sysMon.addScalarMeasurer(m);
    }

    public static void addEnvironmentMeasurer(ASysMon sysMon, AEnvironmentMeasurer m) {
        sysMon.addEnvironmentMeasurer(m);
    }

    public static void addDataSink(ASysMon sysMon, ADataSink handler) {
        sysMon.addDataSink(handler);
    }
}
