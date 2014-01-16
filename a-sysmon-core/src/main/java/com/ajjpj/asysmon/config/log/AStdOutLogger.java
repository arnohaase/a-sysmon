package com.ajjpj.asysmon.config.log;

import com.ajjpj.asysmon.util.AStringFunction;

import java.util.Date;


/**
 * @author arno
 */
public class AStdOutLogger implements ASysMonLogger {
    public static AStdOutLogger INSTANCE = new AStdOutLogger();
    public volatile boolean isDebugEnabled = false;

    @Override public void debug(AStringFunction msg) {
        if(isDebugEnabled) {
            log("DEBUG", msg.apply());
        }
    }

    private void log(String level, String msg) {
        System.out.println(Thread.currentThread().getName() + " " + new Date() + "  " + level + ": " + msg);
    }

    @Override public void info(String msg) {
        log("INFO ", msg);
    }

    @Override public void warn(String msg) {
        log("WARN ", msg);
    }

    @Override public void warn(String msg, Exception exc) {
        warn(msg);
        exc.printStackTrace(System.out);
    }

    @Override public void error(String msg) {
        log("ERROR", msg);
    }

    @Override public void error(String msg, Exception exc) {
        error(msg);
        exc.printStackTrace(System.out);
    }
}
