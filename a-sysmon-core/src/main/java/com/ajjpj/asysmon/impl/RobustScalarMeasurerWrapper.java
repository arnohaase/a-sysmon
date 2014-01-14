package com.ajjpj.asysmon.impl;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class limits the impact on A-SysMon itself if some measurer runs into problems or uses large amounts of resources
 *
 * @author arno
 */
class RobustScalarMeasurerWrapper implements AShutdownable {
    private final AScalarMeasurer inner;
    private final ASysMonLogger log;

    private final long timeoutNanos;
    private final int maxNumTimeouts;

    private final AtomicInteger numTimeouts = new AtomicInteger(0);

    private interface Strategy {
        void prepareMeasurements(Map<String, Object> mementos);
        void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos);
    }

    private final Strategy ENABLED = new Strategy() {
        @Override public void prepareMeasurements(Map<String, Object> mementos) {
            try {
                final long start = System.nanoTime();
                inner.prepareMeasurements(mementos);
                handleDuration(System.nanoTime() - start);
            } catch (Exception e) {
                log.warn("Disabling scalar measurer " + inner.getClass().getName() + " because an exception occurred", e);
                strategy = DISABLED;
            }
        }

        private void handleDuration(long durationNanos) {
            if(durationNanos > timeoutNanos) {
                log.warn("Scalar measurer " + inner.getClass().getName() + " timed out (took " + durationNanos + "ns)");
                numTimeouts.incrementAndGet();
                strategy = TIMED_OUT;
            }
        }

        @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
            try {
                final long start = System.nanoTime();
                inner.contributeMeasurements(data, timestamp, mementos);
                handleDuration(System.nanoTime() - start);
            } catch (Exception e) {
                log.warn("Disabling scalar measurer " + inner.getClass().getName() + " because an exception occurred", e);
                strategy = DISABLED;
            }
        }
    };

    private final Strategy TIMED_OUT = new Strategy() {
        @Override public void prepareMeasurements(Map<String, Object> mementos) {
            try {
                final long start = System.nanoTime();
                inner.prepareMeasurements(mementos);
                handleDuration(System.nanoTime() - start);
            } catch (Exception e) {
                log.warn("Disabling scalar measurer " + inner.getClass().getName() + " because an exception occurred", e);
                strategy = DISABLED;
            }
        }

        private void handleDuration(long durationNanos) {
            if(durationNanos > timeoutNanos) {
                if(numTimeouts.incrementAndGet() >= maxNumTimeouts) {
                    log.warn("Scalar measurer " + inner.getClass().getName() + " timed out " + maxNumTimeouts + " times in row - permanently disabling");
                    strategy = DISABLED;
                }
                else {
                    strategy = ENABLED;
                }
            }
        }

        @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
            try {
                final long start = System.nanoTime();
                inner.contributeMeasurements(data, timestamp, mementos);
                handleDuration(System.nanoTime() - start);
            } catch (Exception e) {
                log.warn("Disabling scalar measurer " + inner.getClass().getName() + " because an exception occurred", e);
                strategy = DISABLED;
            }
        }
    };

    private final Strategy DISABLED = new Strategy() {
        @Override public void prepareMeasurements(Map<String, Object> mementos) {
        }

        @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
        }
    };

    private volatile Strategy strategy = ENABLED;

    RobustScalarMeasurerWrapper(AScalarMeasurer inner, ASysMonLogger log, long timeoutNanos, int maxNumTimeouts) {
        this.inner = inner;
        this.log = log;
        this.timeoutNanos = timeoutNanos;
        this.maxNumTimeouts = maxNumTimeouts;
    }

    public void prepareMeasurements(Map<String, Object> mementos) {
        strategy.prepareMeasurements(mementos);
    }

    public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
        strategy.contributeMeasurements(data, timestamp, mementos);
    }

    @Override
    public void shutdown() throws Exception {
        inner.shutdown();
    }
}
