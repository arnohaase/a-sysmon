package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.asysmon.util.AList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author arno
 */
public class AEnvVarEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_ENV_VAR = "envvar";

    @Override public void contributeMeasurements(Map<AList<String>, AEnvironmentData> data) {
        final Map<String, String> env = System.getenv();

        for(String envName: env.keySet()) {
            final AList<String> key = AList.create(KEY_ENV_VAR, envName);
            data.put(key, new AEnvironmentData(key, env.get(envName)));
        }
    }
}
