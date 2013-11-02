package com.ajjpj.asysmon.config.log;

import com.ajjpj.asysmon.util.AStringFunction;

/**
 * @author arno
 */
public class AStdOutLogger implements ASysMonLogger {
    public static AStdOutLogger INSTANCE = new AStdOutLogger();
    public volatile boolean isDebugEnabled = false;

    @Override public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override public void debug(AStringFunction msg) {
        if(isDebugEnabled) {
            log("DEBUG", msg.apply());
        }
    }

    private void log(String level, String msg) {
        System.out.println(level + ": " + msg); //TODO log timestamp etc.
    }

    @Override public void warn(String msg) {
        log("WARN ", msg);
    }
}
