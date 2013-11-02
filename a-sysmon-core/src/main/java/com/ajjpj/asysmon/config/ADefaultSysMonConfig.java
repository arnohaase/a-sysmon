package com.ajjpj.asysmon.config;


import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;
import com.ajjpj.asysmon.measure.global.AMemoryMeasurer;
import com.ajjpj.asysmon.measure.global.ASystemLoadMeasurer;
import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.measure.threadpool.AThreadCountMeasurer;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.util.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is used to provide a static, 'global' configuration when ASysMon.get() is used to access a singleton
 *  ASysMon instance. Configuration must be registered by calling static 'set()' or 'add()' methods.<p />
 *
 * All methods in this class are thread-safe.
 *
 * @author arno
 */
public class ADefaultSysMonConfig {
    private static final ASysMonConfigBuilder builder = new ASysMonConfigBuilder()
            .withThreadCount()
            .withGlobalMeasurer(new ASystemLoadMeasurer())
            .withGlobalMeasurer(new AMemoryMeasurer())
            .withGlobalMeasurer(AConnectionCounter.INSTANCE);

    public static void setTimer(ATimer timer) {
        builder.withTimer(timer);
    }

    public static void addHandler(ADataSink handler) {
        builder.withDataSink(handler);
    }

    public static void addGlobalMeasurer(AGlobalMeasurer measurer) {
        builder.withGlobalMeasurer(measurer);
    }

    public static ASysMonConfig get() {
        return builder.buildConfig();
    }
}
