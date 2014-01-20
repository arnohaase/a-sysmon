package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.appinfo.AApplicationInfoProvider;
import com.ajjpj.asysmon.config.presentation.APresentationMenuEntry;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.http.AHttpRequestAnalyzer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.Collections;
import java.util.List;


/**
 * @author arno
 */
public class ASysMonConfig {
    public final AApplicationInfoProvider appInfo;

    public final int averagingDelayForScalarsMillis;

    public final long measurementTimeoutNanos;
    public final int maxNumMeasurementTimeouts;

    public final long dataSinkTimeoutNanos;
    public final int maxNumDataSinkTimeouts;

    public final ATimer timer;
    public final AHttpRequestAnalyzer httpRequestAnalyzer;

    public final List<AEnvironmentMeasurer> initialEnvironmentMeasurers;
    public final List<AScalarMeasurer> initialScalarMeasurers;
    public final List<ADataSink> initialDataSinks;

    public final String defaultPage;
    public final List<APresentationMenuEntry> presentationMenuEntries;

    public ASysMonConfig(AApplicationInfoProvider appInfo,
                         int averagingDelayForScalarsMillis, long measurementTimeoutNanos, int maxNumMeasurementTimeouts, long dataSinkTimeoutNanos, int maxNumDataSinkTimeouts,
                         ATimer timer, AHttpRequestAnalyzer httpRequestAnalyzer,
                         List<AEnvironmentMeasurer> environmentMeasurers, List<AScalarMeasurer> initialScalarMeasurers, List<ADataSink> initialDataSinks,
                         String defaultPage, List<APresentationMenuEntry> presentationMenuEntries) {
        this.appInfo = appInfo;
        this.averagingDelayForScalarsMillis = averagingDelayForScalarsMillis;
        this.measurementTimeoutNanos = measurementTimeoutNanos;
        this.maxNumMeasurementTimeouts = maxNumMeasurementTimeouts;
        this.dataSinkTimeoutNanos = dataSinkTimeoutNanos;
        this.maxNumDataSinkTimeouts = maxNumDataSinkTimeouts;
        this.timer = timer;
        this.httpRequestAnalyzer = httpRequestAnalyzer;
        this.initialEnvironmentMeasurers = Collections.unmodifiableList(environmentMeasurers);
        this.initialScalarMeasurers = Collections.unmodifiableList(initialScalarMeasurers);
        this.initialDataSinks = Collections.unmodifiableList(initialDataSinks);
        this.defaultPage = defaultPage;
        this.presentationMenuEntries = Collections.unmodifiableList(presentationMenuEntries);
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
