package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.util.AStringFunction;

/**
 * @author arno
 */
public class CountingLogger implements ASysMonLogger {
    public int numDebug = 0;
    public int numWarn = 0;
    public int numError = 0;

    @Override public void debug(AStringFunction msg) {
        numDebug += 1;
    }

    @Override public void warn(String msg) {
        numWarn += 1;
    }

    @Override public void error(String s) {
        numError += 1;
    }

    @Override public void warn(String msg, Exception exc) {
        warn(msg);
    }

    @Override public void error(String msg, Exception exc) {
        error(msg);
    }
}
