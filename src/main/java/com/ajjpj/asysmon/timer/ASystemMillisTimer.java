package com.ajjpj.asysmon.timer;

/**
 * @author arno
 */
public class ASystemMillisTimer implements ATimer {
    @Override public long getCurrentNanos() {
        return System.currentTimeMillis() * 1000*1000;
    }
}
