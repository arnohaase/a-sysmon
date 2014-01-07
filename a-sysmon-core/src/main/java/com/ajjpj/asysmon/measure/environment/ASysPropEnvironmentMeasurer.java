package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.asysmon.util.AList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author arno
 */
public class ASysPropEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_SYSTEM_PROPERTY = "sysprop";

    @Override public void contributeMeasurements(Map<AList<String>, AEnvironmentData> data) {
        for(String propName: System.getProperties().stringPropertyNames()) {
            final AList<String> key = AList.create(KEY_SYSTEM_PROPERTY, propName);
            data.put(key, new AEnvironmentData(key, System.getProperties().getProperty(propName)));
        }
    }
}
