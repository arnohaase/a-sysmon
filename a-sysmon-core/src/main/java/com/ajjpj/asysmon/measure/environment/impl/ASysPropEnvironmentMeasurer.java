package com.ajjpj.asysmon.measure.environment.impl;

import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;


/**
 * @author arno
 */
public class ASysPropEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_SYSTEM_PROPERTY = "sysprop";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        for(String propName: System.getProperties().stringPropertyNames()) {
            data.add(System.getProperty(propName), KEY_SYSTEM_PROPERTY, propName);
        }
    }

    @Override
    public void shutdown() throws Exception {
    }
}
