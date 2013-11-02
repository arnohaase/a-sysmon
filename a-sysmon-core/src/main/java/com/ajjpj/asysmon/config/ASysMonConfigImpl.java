package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author arno
 */
public class ASysMonConfigImpl implements ASysMonConfig {
    private final ATimer timer;
    private final List<? extends ADataSink> handlers;
    private final List<? extends AGlobalMeasurer> globalMeasurers;

    public ASysMonConfigImpl(ATimer timer, List<? extends ADataSink> handlers, List<? extends AGlobalMeasurer> globalMeasurers) {
        this.timer = timer;
        this.handlers = new ArrayList<ADataSink>(handlers);
        this.globalMeasurers = new ArrayList<AGlobalMeasurer>(globalMeasurers);
    }

    @Override public ATimer getTimer() {
        return timer;
    }

    @Override public List<? extends ADataSink> getHandlers() {
        return handlers;
    }

    @Override public List<? extends AGlobalMeasurer> getGlobalMeasurers() {
        return globalMeasurers;
    }
}

