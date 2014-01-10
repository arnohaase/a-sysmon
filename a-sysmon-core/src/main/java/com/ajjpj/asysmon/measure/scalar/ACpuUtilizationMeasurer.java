package com.ajjpj.asysmon.measure.scalar;

import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.util.AFunction1;
import com.ajjpj.asysmon.util.io.AFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author arno
 */
public class ACpuUtilizationMeasurer implements AScalarMeasurer {
    public static final AFile PROC_STAT_FILE = new AFile("/proc/stat");

    public static final String KEY_PREFIX = "cpu-util:";
    public static final String KEY_MEMENTO = KEY_PREFIX;

    @Override public void prepareMeasurements(Map<String, Object> mementos) throws IOException {
        mementos.put(KEY_MEMENTO, createSnapshot());
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) throws IOException {
        final Map<String, Snapshot> allCurrent = createSnapshot();
        @SuppressWarnings("unchecked")
        final Map<String, Snapshot> allPrev = (Map<String, Snapshot>) mementos.get(KEY_MEMENTO);

        final int numCpus = allCurrent.size() - 1;
        for(String cpu: allCurrent.keySet()) {
            final Snapshot current = allCurrent.get(cpu);
            final Snapshot prev = allPrev.get(cpu);

            final long diffTime = current.timestamp - prev.timestamp;
            if(diffTime <= 0) {
                return;
            }

            final long idleJiffies   = current.idle   - prev.idle;
            final long stolenJiffies = current.stolen - prev.stolen;

            // 'baseline' - 100% for a single CPU, <# cpus>*100% for 'total'
            final long fullJiffies = diffTime * ("cpu".equals(cpu) ? numCpus : 1) / 10;

            // reduce the theoretical 'full' capacity by 'stolen' cycles
            final long availJiffies = fullJiffies - stolenJiffies;

            final long usedJiffies = availJiffies - idleJiffies;

            final long usedPerMill = usedJiffies * 1000 / availJiffies;

            final String key = getUsedKey(cpu);
            data.put(key, new AScalarDataPoint(timestamp, key, usedPerMill, 1));
        }
    }

    public static String getUsedKey(String cpu) {
        return KEY_PREFIX + cpu + ":used%";
    }

    private Map<String, Snapshot> createSnapshot() throws IOException {
        return PROC_STAT_FILE.iterate(Charset.defaultCharset(), new AFunction1<Iterator<String>, Map<String, Snapshot>, RuntimeException>() {
            @Override public Map<String, Snapshot> apply(Iterator<String> iter) {
                final Map<String, Snapshot> result = new HashMap<String, Snapshot>();

                while(iter.hasNext()) {
                    final String line = iter.next();
                    final String[] split = line.split("\\s+");

                    if(split[0].startsWith("cpu")) {
                        final long idle = Long.valueOf(split[4]);
                        final long stolen = split.length >= 8 ? Long.valueOf(split[8]) : 0;
                        result.put(split[0], new Snapshot(idle, stolen));
                    }
                }

                return result;
            }
        });
    }

    @Override public void shutdown() throws Exception {
    }

    static class Snapshot {
        public final long timestamp = System.currentTimeMillis();
        public final long idle;
        public final long stolen;

        Snapshot(long idle, long stolen) {
            this.idle = idle;
            this.stolen = stolen;
        }
    }
}
