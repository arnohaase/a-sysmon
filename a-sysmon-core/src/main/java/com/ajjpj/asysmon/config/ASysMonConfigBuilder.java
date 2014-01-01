package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.log.ALog4JLogger;
import com.ajjpj.asysmon.config.log.AStdOutLogger;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.timer.ASystemNanoTimer;
import com.ajjpj.asysmon.util.timer.ATimer;

import java.util.ArrayList;
import java.util.List;


/**
 * @author arno
 */
public class ASysMonConfigBuilder {
    private String applicationId;
    private String applicationVersionId;
    private String applicationInstanceId;
    private String applicationInstanceHtmlColorCode;

    private ASysMonLogger logger = defaultLogger();
    private ATimer timer = new ASystemNanoTimer();

    private boolean implicitlyShutDownWithServlet = true;

    private final List<AScalarMeasurer> scalarMeasurers = new ArrayList<AScalarMeasurer>();
    private final List<ADataSink> dataSinks = new ArrayList<ADataSink>();

    public ASysMonConfigBuilder(String applicationId, String applicationVersionId, String applicationInstanceId, String applicationInstanceHtmlColorCode) {
        this.applicationId = applicationId;
        this.applicationVersionId = applicationVersionId;
        this.applicationInstanceId = applicationInstanceId;
        this.applicationInstanceHtmlColorCode = applicationInstanceHtmlColorCode;
    }

    public ASysMonConfigBuilder setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }
    public ASysMonConfigBuilder setApplicationVersionId(String applicationVersionId) {
        this.applicationVersionId = applicationVersionId;
        return this;
    }
    public ASysMonConfigBuilder setApplicationInstanceId(String applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
        return this;
    }
    public ASysMonConfigBuilder setApplicationInstanceHtmlColorCode(String applicationInstanceHtmlColorCode) {
        this.applicationInstanceHtmlColorCode = applicationInstanceHtmlColorCode;
        return this;
    }

    public ASysMonConfigBuilder setLogger(ASysMonLogger logger) {
        this.logger = logger;
        return this;
    }
    public ASysMonConfigBuilder setTimer(ATimer timer) {
        this.timer = timer;
        return this;
    }

    public ASysMonConfigBuilder setImplicitlyShutDownWithServlet(boolean implicitlyShutDownWithServlet) {
        this.implicitlyShutDownWithServlet = implicitlyShutDownWithServlet;
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

    public ASysMonConfig build() {
        return new ASysMonConfig(
                applicationId, applicationVersionId, applicationInstanceId, applicationInstanceHtmlColorCode,
                logger, timer,
                implicitlyShutDownWithServlet,
                scalarMeasurers, dataSinks);
    }

    private static ASysMonLogger defaultLogger() {
        try {
            return ALog4JLogger.INSTANCE; //TODO verify that this works without log4j
        }
        catch (Throwable th) {
            return AStdOutLogger.INSTANCE;
        }
    }
}
