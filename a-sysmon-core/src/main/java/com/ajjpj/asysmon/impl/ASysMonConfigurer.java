package com.ajjpj.asysmon.impl;


import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;

/**
 * This class can change the configuration of an existing ASysMon instance. This is done to avoid race conditions
 *  during application startup, e.g. if ASysMon is used during Spring startup, and servlets need to contribute
 *  configuration later.<p>
 *
 * These methods are not part of ASysMon itself to keep that API lean and clean: These methods are for use during system
 *  initialization.
 *
 * @author arno
 */
public class ASysMonConfigurer {
    public static void addScalarMeasurer(ASysMonApi sysMon, AScalarMeasurer m) {
        ((ASysMonImpl) sysMon).addScalarMeasurer(m);
    }

    public static void addEnvironmentMeasurer(ASysMonApi sysMon, AEnvironmentMeasurer m) {
        ((ASysMonImpl) sysMon).addEnvironmentMeasurer(m);
    }

    public static void addDataSink(ASysMonApi sysMon, ADataSink handler) {
        ((ASysMonImpl) sysMon).addDataSink(handler);
    }
}
