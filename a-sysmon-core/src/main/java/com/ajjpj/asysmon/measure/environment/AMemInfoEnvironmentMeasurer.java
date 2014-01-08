package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.asysmon.util.AList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author arno
 */
public class AMemInfoEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_MEMINFO = "memory";

    @Override public void contributeMeasurements(Map<AList<String>, AEnvironmentData> data) throws IOException {
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

                final AList<String> fullKey = AList.create(ACpuEnvironmentMeasurer.KEY_HW, KEY_MEMINFO, key);
                data.put(fullKey, new AEnvironmentData(fullKey, value));
            }
        }
        finally {
            br.close();
        }
    }
}
