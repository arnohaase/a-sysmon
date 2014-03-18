package com.ajjpj.asysmon.measure.scalar;

import com.ajjpj.abase.io.AFile;
import com.ajjpj.asysmon.data.AScalarDataPoint;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * @author arno
 */
public class AProcSelfStatMeasurer implements AScalarMeasurer {
    public static final AFile PROC_SELF_STAT_FILE = new AFile("/proc/self/stat", Charset.defaultCharset());
    public static final String KEY_MEMENTO = "proc:self:stat";

    public static final String KEY_SELF_USER = ACpuUtilizationMeasurer.KEY_PREFIX + "self-user";
    public static final String KEY_SELF_KERNEL = ACpuUtilizationMeasurer.KEY_PREFIX + "self-kernel";

    @Override public void prepareMeasurements(Map<String, Object> mementos) throws Exception {
        mementos.put(KEY_MEMENTO, createSnapshot());
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) throws Exception {
        final Snapshot prev = (Snapshot) mementos.get(KEY_MEMENTO);
        final Snapshot current = createSnapshot();

        final long diffTime = current.timestamp - prev.timestamp;

        final long userJiffies = current.userJiffies - prev.userJiffies;
        final long kernelJiffies = current.kernelJiffies - prev.kernelJiffies;

        final long userPerMill = userJiffies * 10*1000 / diffTime;
        final long kernelPerMill = kernelJiffies * 10*1000 / diffTime;

        data.put(KEY_SELF_USER, new AScalarDataPoint(timestamp, KEY_SELF_USER, userPerMill, 1));
        data.put(KEY_SELF_KERNEL, new AScalarDataPoint(timestamp, KEY_SELF_KERNEL, kernelPerMill, 1));
    }

    @Override public void shutdown() throws Exception {
    }

    private Snapshot createSnapshot() throws IOException {
        final String raw = PROC_SELF_STAT_FILE.lines().get(0);
        final String[] split = raw.split("\\s+");

        final long userJiffies = Long.valueOf(split[13]);
        final long kernelJiffies = Long.valueOf(split[14]);

        return new Snapshot(userJiffies, kernelJiffies);
    }

    private static class Snapshot {
        final long timestamp = System.currentTimeMillis();
        final long userJiffies;
        final long kernelJiffies;

        Snapshot(long userJiffies, long kernelJiffies) {
            this.userJiffies = userJiffies;
            this.kernelJiffies = kernelJiffies;
        }
    }
}
