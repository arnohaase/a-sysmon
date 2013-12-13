package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.log.ALog4JLogger;
import com.ajjpj.asysmon.config.log.AStdOutLogger;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.measure.scalar.ASimpleMemoryMeasurer;
import com.ajjpj.asysmon.measure.scalar.ASystemLoadMeasurer;
import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.util.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author arno
 */
public class AGlobalConfig {
    private static volatile ATimer timer = new ASystemNanoTimer();
    private static volatile ASysMonLogger logger = defaultLogger();
    private static volatile boolean implicitlyShutDownWithServlet = true;

    private static List<AScalarMeasurer> scalarMeasurers = new CopyOnWriteArrayList<AScalarMeasurer>();

    static {
        scalarMeasurers.add(new ASystemLoadMeasurer());
        scalarMeasurers.add(new ASimpleMemoryMeasurer());
        scalarMeasurers.add(AConnectionCounter.INSTANCE);
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

    public static List<AScalarMeasurer> getScalarMeasurers() {
        return scalarMeasurers;
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
