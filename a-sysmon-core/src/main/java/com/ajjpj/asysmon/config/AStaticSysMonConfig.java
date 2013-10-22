package com.ajjpj.asysmon.config;


import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.timer.ATimer;

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
public class AStaticSysMonConfig {
    private static volatile ATimer timer = new ASystemNanoTimer();
    private static List<ADataSink> handlers = new CopyOnWriteArrayList<ADataSink>();

    public static void setTimer(ATimer timer) {
        AStaticSysMonConfig.timer = timer;
    }

    public static void addHandler(ADataSink handler) {
        AStaticSysMonConfig.handlers.add(handler);
    }

    public static ASysMonConfig get() {
        return new ASysMonConfig() {
            @Override public ATimer getTimer() {
                return timer;
            }

            @Override
            public List<ADataSink> getHandlers() {
                return handlers;
            }
        };
    }
}
