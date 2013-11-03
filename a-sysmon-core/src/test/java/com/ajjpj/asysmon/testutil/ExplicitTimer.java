package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.util.timer.ATimer;

/**
 * @author arno
 */
public class ExplicitTimer implements ATimer {
    public long curNanos = 0;

    @Override public long getCurrentNanos() {
        return curNanos;
    }
}
