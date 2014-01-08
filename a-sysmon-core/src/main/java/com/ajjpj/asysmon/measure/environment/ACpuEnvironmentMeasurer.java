package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.asysmon.util.AList;

import java.io.*;
import java.util.Map;


/**
 * @author arno
 */
public class ACpuEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_HW = "hw";
    public static final String KEY_CPUS = "cpus";

    @Override public void contributeMeasurements(Map<AList<String>, AEnvironmentData> data) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
        try {
            String line;
            String cpuKey = "cpu";
            int numCpus = 0;
            while ((line = br.readLine()) != null) {
                final int idxColon = line.indexOf(':');
                if(idxColon < 0) {
                    continue;
                }

                final String key = line.substring(0, idxColon).trim();
                final String value = line.substring(idxColon+1).trim();

                if("processor".equals(key)) {
                    cpuKey = "cpu " + value;
                    numCpus += 1;
                    continue;
                }

                final AList<String> fullKey = AList.create(KEY_HW, KEY_CPUS, cpuKey, key);
                data.put(fullKey, new AEnvironmentData(fullKey, value));
            }

            final AList<String> cpuNumKey = AList.create(KEY_HW, KEY_CPUS);
            data.put(cpuNumKey, new AEnvironmentData(cpuNumKey, String.valueOf(numCpus)));
        }
        finally {
            br.close();
        }
    }
}

