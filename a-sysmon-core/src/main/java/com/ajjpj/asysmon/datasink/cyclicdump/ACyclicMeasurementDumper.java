package com.ajjpj.asysmon.datasink.cyclicdump;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * This class cyclically dumps all scalar measurements, e.g. to Log4J.<p />
 *
 * Calling the constructor takes care of registration with ASysMon. <p />
 *
 * This is a data sink for pragmatic reasons, 'shutdown' integration in particular. It does not actually use hierarchical
 *  measurements.
 *
 * @author arno
 */
public abstract class ACyclicMeasurementDumper implements ADataSink {
    private final ScheduledExecutorService ec;
    private final ASysMon sysMon;

    private final Runnable dumper = new Runnable() {
        @Override public void run() {
            try {
                final Map<String, AGlobalDataPoint> m = sysMon.getGlobalMeasurements();
                for(String key: m.keySet()) {
                    dump("Scalar Measurement: " + key + " = " + m.get(key).getFormattedValue());
                }
            }
            catch(Exception exc) {
                exc.printStackTrace();
                dump(exc.toString());
            }
        }
    };

    public ACyclicMeasurementDumper(ASysMon sysMon, int frequencyInSeconds) {
        this(sysMon, 0, frequencyInSeconds);
    }

    public ACyclicMeasurementDumper(ASysMon sysMon, int initialDelaySeconds, int frequencyInSeconds) {
        ec = Executors.newSingleThreadScheduledExecutor();
        ec.scheduleAtFixedRate(dumper, initialDelaySeconds, frequencyInSeconds, TimeUnit.SECONDS);

        this.sysMon = sysMon;
        ASysMonConfigurer.addDataSink(sysMon, this);
    }

    protected abstract void dump(String s);

    @Override public void onStartedHierarchicalMeasurement() {
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
    }

    @Override public void shutdown() {
        ec.shutdown();
    }
}
