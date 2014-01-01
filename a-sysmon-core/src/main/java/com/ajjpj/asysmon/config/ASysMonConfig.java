package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.Collections;
import java.util.List;


/**
 * @author arno
 */
public class ASysMonConfig {
    public final String applicationId;
    public final String applicationVersionId;
    public final String applicationInstanceId;
    public final String applicationInstanceHtmlColorCode;

    public final ASysMonLogger logger;
    public final ATimer timer;

    public final boolean implicitlyShutDownWithServlet;

    public final List<AScalarMeasurer> initialScalarMeasurers;
    public final List<ADataSink> initialDataSinks;

    public ASysMonConfig(String applicationId, String applicationVersionId, String instanceId, String instanceHtmlColorCode, ASysMonLogger logger, ATimer timer, boolean implicitlyShutDownWithServlet, List<AScalarMeasurer> initialScalarMeasurers, List<ADataSink> initialDataSinks) {
        this.applicationId = applicationId;
        this.applicationVersionId = applicationVersionId;
        this.applicationInstanceId = instanceId;
        this.applicationInstanceHtmlColorCode = instanceHtmlColorCode;
        this.logger = logger;
        this.timer = timer;
        this.implicitlyShutDownWithServlet = implicitlyShutDownWithServlet;
        this.initialScalarMeasurers = Collections.unmodifiableList(initialScalarMeasurers);
        this.initialDataSinks = Collections.unmodifiableList(initialDataSinks);
    }

    /**
     * This flag switches off all 'risky' (or potentially expensive) functionality. It serves as a safeguard in case
     *  A-SysMon has a bug that impacts an application.
     */
    public boolean isGloballyDisabled() {
        final String s = System.getProperty("com.ajjpj.asysmon.globallydisabled");
        return "true".equals(s);
    }
}
