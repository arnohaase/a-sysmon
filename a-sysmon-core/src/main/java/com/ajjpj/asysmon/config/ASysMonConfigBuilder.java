package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.appinfo.AApplicationInfoProvider;
import com.ajjpj.asysmon.config.log.*;
import com.ajjpj.asysmon.config.presentation.APresentationMenuEntry;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.http.AHttpRequestAnalyzer;
import com.ajjpj.asysmon.measure.http.ASimpleHttpRequestAnalyzer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author arno
 */
public class ASysMonConfigBuilder {
    private AApplicationInfoProvider appInfo;

    private int averagingDelayForScalarsMillis = 1000;

    private long measurementTimeoutNanos = 20*1000*1000;
    private int maxNumMeasurementTimeouts = 3;

    private long dataSinkTimeoutNanos = 100*1000;
    private int maxNumDataSinkTimeouts = 3;

    private ATimer timer = new ASystemNanoTimer();
    private AHttpRequestAnalyzer httpRequestAnalyzer = new ASimpleHttpRequestAnalyzer();

    private final List<AEnvironmentMeasurer> environmentMeasurers = new ArrayList<AEnvironmentMeasurer>();
    private final List<AScalarMeasurer> scalarMeasurers = new ArrayList<AScalarMeasurer>();
    private final List<ADataSink> dataSinks = new ArrayList<ADataSink>();

    private final List<APresentationMenuEntry> presentationMenuEntries = new ArrayList<APresentationMenuEntry>();
    private String defaultPage;

    public ASysMonConfigBuilder(AApplicationInfoProvider appInfo) {
        this.appInfo = appInfo;
    }

    @SuppressWarnings("unused")
    public ASysMonConfigBuilder setApplicationInfo(AApplicationInfoProvider appInfo) {
        this.appInfo = appInfo;
        return this;
    }

    public ASysMonConfigBuilder setTimer(ATimer timer) {
        this.timer = timer;
        return this;
    }

    public ASysMonConfigBuilder setAveragingDelayForScalarsMillis(int averagingDelayForScalarsMillis) {
        this.averagingDelayForScalarsMillis = averagingDelayForScalarsMillis;
        return this;
    }

    public ASysMonConfigBuilder setMeasurementTimeoutNanos(long measurementTimeoutNanos) {
        this.measurementTimeoutNanos = measurementTimeoutNanos;
        return this;
    }

    public ASysMonConfigBuilder setHttpRequestAnalyzer(AHttpRequestAnalyzer httpRequestAnalyzer) {
        this.httpRequestAnalyzer = httpRequestAnalyzer;
        return this;
    }

    public ASysMonConfigBuilder setMaxNumMeasurementTimeouts(int maxNumMeasurementTimeouts) {
        this.maxNumMeasurementTimeouts = maxNumMeasurementTimeouts;
        return this;
    }

    public ASysMonConfigBuilder setDataSinkTimeoutNanos(long dataSinkTimeoutNanos) {
        this.dataSinkTimeoutNanos = dataSinkTimeoutNanos;
        return this;
    }

    public ASysMonConfigBuilder setMaxNumDataSinkTimeouts(int maxNumDataSinkTimeouts) {
        this.maxNumDataSinkTimeouts = maxNumDataSinkTimeouts;
        return this;
    }

    public ASysMonConfigBuilder addEnvironmentMeasurer(AEnvironmentMeasurer environmentMeasurer) {
        this.environmentMeasurers.add(environmentMeasurer);
        return this;
    }

    public ASysMonConfigBuilder addScalarMeasurer(AScalarMeasurer scalarMeasurer) {
        this.scalarMeasurers.add(scalarMeasurer);
        return this;
    }

    public ASysMonConfigBuilder addDataSink(ADataSink dataSink) {
        this.dataSinks.add(dataSink);
        return this;
    }

    public ASysMonConfigBuilder setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
        return this;
    }

    public ASysMonConfigBuilder addPresentationMenuEntry(String label, List<APresentationPageDefinition> entries) {
        presentationMenuEntries.add(new APresentationMenuEntry(label, entries));
        return this;
    }

    @SuppressWarnings("unused")
    public ASysMonConfigBuilder addPresentationMenuEntry(String label, APresentationPageDefinition... entries) {
        return addPresentationMenuEntry(label, Arrays.asList(entries));
    }

    public ASysMonConfig build() {
        return new ASysMonConfig(
                appInfo,
                averagingDelayForScalarsMillis,
                measurementTimeoutNanos, maxNumMeasurementTimeouts,
                dataSinkTimeoutNanos, maxNumDataSinkTimeouts,
                timer, httpRequestAnalyzer,
                environmentMeasurers, scalarMeasurers, dataSinks,
                defaultPage, presentationMenuEntries
                );
    }
}
