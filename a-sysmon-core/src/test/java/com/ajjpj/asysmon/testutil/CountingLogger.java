package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.util.AStringFunction;

/**
 * @author arno
 */
public class CountingLogger extends ASysMonLogger {
    public int numDebug = 0;
    public int numInfo = 0;
    public int numWarn = 0;
    public int numError = 0;

    public void reset() {
        numDebug = 0;
        numInfo = 0;
        numWarn = 0;
        numError = 0;
    }

    @Override public void debug(AStringFunction msg) {
        numDebug += 1;
    }

    @Override public void info(String msg) {
        numInfo += 1;
    }

    @Override public void warn(String msg) {
        numWarn += 1;
    }

    @Override public void warn(String msg, Exception exc) {
        warn(msg);
    }

    @Override public void error(String s) {
        numError += 1;
    }

    @Override public void error(Exception exc) {
        numError += 1;
    }

    @Override public void error(String msg, Exception exc) {
        error(msg);
    }
}
