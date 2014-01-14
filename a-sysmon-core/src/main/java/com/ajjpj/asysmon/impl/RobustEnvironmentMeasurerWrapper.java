package com.ajjpj.asysmon.impl;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class limits the impact on A-SysMon itself if some measurer runs into problems or uses large amounts of resources
 *
 * @author arno
 */
class RobustEnvironmentMeasurerWrapper implements AShutdownable {
    private final AEnvironmentMeasurer inner;
    private final ASysMonLogger log;

    private final long timeoutNanos;
    private final int maxNumTimeouts;

    private final AtomicInteger numTimeouts = new AtomicInteger(0);

    private interface Strategy {
        void contributeMeasurements(AEnvironmentMeasurer.EnvironmentCollector data);
    }

    private final Strategy ENABLED = new Strategy() {
        @Override public void contributeMeasurements(AEnvironmentMeasurer.EnvironmentCollector data) {
            try {
                final long start = System.nanoTime();
                inner.contributeMeasurements(data);
                handleDuration(System.nanoTime() - start);
            } catch (Exception e) {
                log.warn("Disabling scalar measurer " + inner.getClass().getName() + " because an exception occurred", e);
                strategy = DISABLED;
            }
        }

        private void handleDuration(long durationNanos) {
            if(durationNanos > timeoutNanos) {
                log.warn("Environment measurer " + inner.getClass().getName() + " timed out (took " + durationNanos + "ns)");
                numTimeouts.incrementAndGet();
                strategy = TIMED_OUT;
            }
        }
    };

    private final Strategy TIMED_OUT = new Strategy() {
        @Override public void contributeMeasurements(AEnvironmentMeasurer.EnvironmentCollector data) {
            try {
                final long start = System.nanoTime();
                inner.contributeMeasurements(data);
                handleDuration(System.nanoTime() - start);
            } catch (Exception e) {
                log.warn("Disabling scalar measurer " + inner.getClass().getName() + " because an exception occurred", e);
                strategy = DISABLED;
            }
        }

        private void handleDuration(long durationNanos) {
            if(durationNanos > timeoutNanos) {
                if(numTimeouts.incrementAndGet() >= maxNumTimeouts) {
                    log.warn("Environment measurer " + inner.getClass().getName() + " timed out " + maxNumTimeouts + " times in row - permanently disabling");
                    strategy = DISABLED;
                }
                else {
                    strategy = ENABLED;
                }
            }
        }
    };

    private final Strategy DISABLED = new Strategy() {
        @Override public void contributeMeasurements(AEnvironmentMeasurer.EnvironmentCollector data) {
        }
    };

    private volatile Strategy strategy = ENABLED;

    RobustEnvironmentMeasurerWrapper(AEnvironmentMeasurer inner, ASysMonLogger log, long timeoutNanos, int maxNumTimeouts) {
        this.inner = inner;
        this.log = log;
        this.timeoutNanos = timeoutNanos;
        this.maxNumTimeouts = maxNumTimeouts;
    }

    void contributeMeasurements(AEnvironmentMeasurer.EnvironmentCollector data) {
        strategy.contributeMeasurements(data);
    }

    @Override
    public void shutdown() throws Exception {
        inner.shutdown();
    }
}
