package com.ajjpj.asysmon.datasink.cyclicdump;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.config.ASysMonAware;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * This class cyclically dumps all environment measurements, e.g. to Log4J.<p />
 *
 * Calling the constructor takes care of registration with ASysMon. <p />
 *
 * This is a data sink for pragmatic reasons, 'shutdown' integration in particular. It does not actually use hierarchical
 *  measurements.
 *
 * @author arno
 */
public abstract class ACyclicMeasurementDumper implements ADataSink, ASysMonAware {
    private static final ASysMonLogger log = ASysMonLogger.get(ACyclicMeasurementDumper.class);

    private final ScheduledExecutorService ec;
    private volatile ASysMonApi sysMon;
    private final int initialDelaySeconds;
    private final int frequencyInSeconds;
    private final int averagingDelayMillis;

    private final Runnable dumper = new Runnable() {
        @Override public void run() {
            try {
                final Map<String, AScalarDataPoint> m = sysMon.getScalarMeasurements(averagingDelayMillis);
                for(String key: m.keySet()) {
                    dump("Scalar Measurement: " + key + " = " + m.get(key).getFormattedValue());
                }
            }
            catch(Exception exc) {
                log.error(exc);
                dump(exc.toString());
            }
        }
    };

    public ACyclicMeasurementDumper(int initialDelaySeconds, int frequencyInSeconds, int averagingDelayMillis) {
        this.initialDelaySeconds = initialDelaySeconds;
        this.frequencyInSeconds = frequencyInSeconds;
        this.averagingDelayMillis = averagingDelayMillis;
        ec = Executors.newSingleThreadScheduledExecutor();
    }

    @Override public void setASysMon(ASysMonApi sysMon) {
        this.sysMon = sysMon;
        ec.scheduleAtFixedRate(dumper, initialDelaySeconds, frequencyInSeconds, TimeUnit.SECONDS);
    }

    protected abstract void dump(String s);

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
    }

    @Override public void shutdown() {
        ec.shutdown();
    }
}
