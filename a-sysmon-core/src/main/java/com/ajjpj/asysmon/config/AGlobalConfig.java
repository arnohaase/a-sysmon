package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.log.ALog4JLogger;
import com.ajjpj.asysmon.config.log.AStdOutLogger;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;
import com.ajjpj.asysmon.measure.global.AMemoryMeasurer;
import com.ajjpj.asysmon.measure.global.ASystemLoadMeasurer;
import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.util.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author arno
 */
public class AGlobalConfig {
    private static volatile ATimer timer = new ASystemNanoTimer();
    private static volatile ASysMonLogger logger = defaultLogger();
    private static volatile boolean implicitlyShutDownWithServlet = true;

    private static List<AGlobalMeasurer> globalMeasurers = new CopyOnWriteArrayList<AGlobalMeasurer>();

    static {
        globalMeasurers.add(new ASystemLoadMeasurer());
        globalMeasurers.add(new AMemoryMeasurer());
        globalMeasurers.add(AConnectionCounter.INSTANCE);
    }


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

    public static void setLogger(ASysMonLogger newLogger) {
        logger = newLogger;
    }

    public static ATimer getTimer() {
        return timer;
    }

    public static void setTimer(ATimer timer) {
        AGlobalConfig.timer = timer;
    }

    public static List<AGlobalMeasurer> getGlobalMeasurers() {
        return globalMeasurers;
    }

    public static boolean getImplicitlyShutDownWithServlet() {
        return implicitlyShutDownWithServlet;
    }

    public static void setImplicitlyShutDownWithServlet(boolean implicitlyShutDownWithServlet) {
        AGlobalConfig.implicitlyShutDownWithServlet = implicitlyShutDownWithServlet;
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
