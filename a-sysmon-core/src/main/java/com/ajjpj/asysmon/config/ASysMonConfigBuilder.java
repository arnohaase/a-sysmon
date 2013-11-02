package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;
import com.ajjpj.asysmon.measure.threadpool.AThreadCountMeasurer;
import com.ajjpj.asysmon.util.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is synchronized to be prepared for the unusual situation of several threads contributing to a
 *  configuration. Building a config is not time critical, so we want to be "better safe than sorry" here.
 *
 * @author arno
 */
public class ASysMonConfigBuilder {
    private ATimer timer = new ASystemNanoTimer();
    private List<ADataSink> handlers = new ArrayList<ADataSink>();
    private List<AGlobalMeasurer> globalMeasurers = new ArrayList<AGlobalMeasurer>();

    public synchronized ASysMonConfigBuilder withTimer(ATimer timer) {
        this.timer = timer;
        return this;
    }

    public synchronized ASysMonConfigBuilder withDataSink(ADataSink handler) {
        handlers.add(handler);
        return this;
    }

    public synchronized ASysMonConfigBuilder withGlobalMeasurer(AGlobalMeasurer measurer) {
        globalMeasurers.add(measurer);
        return this;
    }

    /**
     * This is a convenience method to register thread count in all relevant places.
     */
    public synchronized ASysMonConfigBuilder withThreadCount() {
        final AThreadCountMeasurer threadCountMeasurer = new AThreadCountMeasurer();
        return withDataSink(threadCountMeasurer.counter).
               withGlobalMeasurer(threadCountMeasurer);
    }

    public synchronized ASysMonConfig buildConfig() {
        return new ASysMonConfigImpl(timer, handlers, globalMeasurers);
    }

    public synchronized ASysMon build() {
        return new ASysMon(buildConfig());
    }
}
