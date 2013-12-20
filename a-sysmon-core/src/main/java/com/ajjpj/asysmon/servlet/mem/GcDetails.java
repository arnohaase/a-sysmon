package com.ajjpj.asysmon.servlet.mem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author arno
 */
class GcDetails {
    final long startMillis;
    final long durationNanos;
    final String gcType;

    final String cause;
    final String algorithm;

    final List<GcMemDetails> memDetails = new ArrayList<GcMemDetails>();

    GcDetails(long startMillis, long durationNanos, String gcType, String cause, String algorithm) {
        this.startMillis = startMillis;
        this.durationNanos = durationNanos;
        this.gcType = gcType;
        this.cause = cause;
        this.algorithm = algorithm;
    }
}
