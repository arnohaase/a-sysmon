package com.ajjpj.asysmon.measure.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author arno
 */
public class AMemInfoEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_MEMINFO = "memory";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        final BufferedReader br = new BufferedReader(new FileReader(new File("/proc/meminfo")));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                final int idxColon = line.indexOf(':');
                if(idxColon < 0) {
                    continue;
                }

                final String key = line.substring(0, idxColon).trim();
                final String value = line.substring(idxColon+1).trim();

                data.add(value, ACpuEnvironmentMeasurer.KEY_HW, KEY_MEMINFO, key);
            }
        }
        finally {
            br.close();
        }
    }
}
