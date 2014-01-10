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
    public static final String KEY_AVAILABLE = KEY_PREFIX + "available";
    public static final String KEY_ALL_USED = KEY_PREFIX + "all-used";

    @Override public void prepareMeasurements(Map<String, Object> mementos) throws IOException {
        mementos.put(KEY_MEMENTO, createSnapshot());
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) throws IOException {
        final Map<String, Snapshot> allCurrent = createSnapshot();
        @SuppressWarnings("unchecked")
        final Map<String, Snapshot> allPrev = (Map<String, Snapshot>) mementos.get(KEY_MEMENTO);

        final int numCpus = allCurrent.size() - 1;
        final Snapshot current = allCurrent.get("cpu");
        final Snapshot prev = allPrev.get("cpu");

        final long diffTime = current.timestamp - prev.timestamp;
        if(diffTime <= 0) {
            return;
        }

        final long idleJiffies   = current.idle   - prev.idle;
        final long stolenJiffies = current.stolen - prev.stolen;

        // 'baseline' - 100% for a single CPU, <# cpus>*100% for 'total'
        final long fullJiffies = diffTime * numCpus / 10;

        // reduce the theoretical 'full' capacity by 'stolen' cycles
        final long availJiffies = fullJiffies - stolenJiffies;

        final long usedJiffies = availJiffies - idleJiffies;

        final long usedPerMill = usedJiffies * 10;

        data.put(KEY_AVAILABLE, new AScalarDataPoint(timestamp, KEY_AVAILABLE, availJiffies / (diffTime / 10) * 1000, 1));
        data.put(KEY_ALL_USED, new AScalarDataPoint(timestamp, KEY_ALL_USED, usedPerMill, 1));
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
