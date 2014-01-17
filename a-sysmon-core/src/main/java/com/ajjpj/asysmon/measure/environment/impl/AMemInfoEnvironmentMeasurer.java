package com.ajjpj.asysmon.measure.environment.impl;

import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.environment.impl.ACpuEnvironmentMeasurer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author arno
 */
public class AMemInfoEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_MEMINFO = "memory";

    public static final String KEY_DETAIL_MEM_TOTAL = "MemTotal";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        final Map<String, String> raw = read();
        for(String key: raw.keySet()) {
            data.add(raw.get(key), ACpuEnvironmentMeasurer.KEY_HW, KEY_MEMINFO, key);
        }
    }

    public Map<String, String> read() throws IOException {
        final Map<String, String> result = new HashMap<String, String>();
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

                result.put(key, value);
            }
        }
        finally {
            br.close();
        }
        return result;
    }

    @Override
    public void shutdown() throws Exception {
    }
}
