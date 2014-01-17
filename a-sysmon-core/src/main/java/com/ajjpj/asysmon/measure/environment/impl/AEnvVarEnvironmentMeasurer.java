package com.ajjpj.asysmon.measure.environment.impl;

import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;

import java.util.Map;

/**
 * @author arno
 */
public class AEnvVarEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_ENV_VAR = "envvar";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        final Map<String, String> env = System.getenv();

        for(String envName: env.keySet()) {
            data.add(env.get(envName), KEY_ENV_VAR, envName);
        }
    }

    @Override
    public void shutdown() throws Exception {
    }
}
