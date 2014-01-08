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

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
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

                if(value.isEmpty()) {
                    continue;
                }

                if("processor".equals(key)) {
                    cpuKey = "cpu " + value;
                    numCpus += 1;
                    continue;
                }

                if("model name".equals(key)) {
                    data.add(value, KEY_HW, KEY_CPUS, cpuKey);
                    continue;
                }

                data.add(value, KEY_HW, KEY_CPUS, cpuKey, key);
            }

            data.add(String.valueOf(numCpus), KEY_HW, KEY_CPUS);
        }
        finally {
            br.close();
        }
    }
}

