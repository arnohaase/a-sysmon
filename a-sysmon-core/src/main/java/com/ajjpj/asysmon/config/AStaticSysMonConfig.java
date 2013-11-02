package com.ajjpj.asysmon.config;


import com.ajjpj.asysmon.config.log.ALog4JLogger;
import com.ajjpj.asysmon.config.log.AStdOutLogger;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
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
public class AStaticSysMonConfig {
    private static final AThreadCountMeasurer threadCountMeasurer = new AThreadCountMeasurer();

    private static volatile ASysMonLogger logger = defaultLogger();
    private static volatile ATimer timer = new ASystemNanoTimer();
    private static final List<ADataSink> handlers = new CopyOnWriteArrayList<ADataSink>(Arrays.asList(threadCountMeasurer.counter));

    //TODO make the list of global measurers configurable
    private static final List<? extends AGlobalMeasurer> globalMeasurers = Arrays.asList(
            new ASystemLoadMeasurer(),
            new AMemoryMeasurer(),
            threadCountMeasurer,
            AConnectionCounter.INSTANCE
    );

    private static ASysMonLogger defaultLogger() {
        try {
            return ALog4JLogger.INSTANCE; //TODO verify that this works without log4j
        }
        catch (Throwable th) {
            return AStdOutLogger.INSTANCE;
        }
    }

    public static ASysMonLogger getLogger() {
        return logger;
    }

    public static void setLogger(ASysMonLogger logger) {
        AStaticSysMonConfig.logger = logger;
    }

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

            @Override public List<? extends ADataSink> getHandlers() {
                return handlers;
            }

            @Override public List<? extends AGlobalMeasurer> getGlobalMeasurers() {
                return globalMeasurers;
            }
        };
    }

    /**
     * This flag switches off all 'risky' (or potentially expensive) functionality. It serves as a safeguard in case
     *  A-SysMon has a bug that impacts an application.
     */
    public static boolean isGloballyDisabled() {
        final String s = System.getProperty("com.ajjpj.asysmon.globallydisabled");
        return "true".equals(s);
    }
}
