package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.data.AThreadBasedData;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a builder class representing an ongoing thread specific measurement.
 *
 * @author arno
 */
public class AMeasurement {
    private final boolean isPartOfParent;

    private final long startTimeMillis; // this is a *nix style timestamp
    private final long startTimeNanos; // this number has no absolute meaning and is useful only for measuring differences
    private final String identifier;

    private final Map<String, String> parameters = new HashMap<String, String>();

    AMeasurement(boolean partOfParent, long startTimeMillis, long startTimeNanos, String identifier) {
        isPartOfParent = partOfParent;
        this.startTimeMillis = startTimeMillis;
        this.startTimeNanos = startTimeNanos;
        this.identifier = identifier;
    }


}
