package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.config.AConfigFactory;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.ASysMonConfigBuilder;
import com.ajjpj.asysmon.measure.environment.*;
import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.measure.scalar.*;
import com.ajjpj.asysmon.servlet.environment.AEnvVarPageDefinition;
import com.ajjpj.asysmon.servlet.environment.AScalarPageDefinition;
import com.ajjpj.asysmon.servlet.memgc.AMemGcPageDefinition;
import com.ajjpj.asysmon.servlet.performance.bottomup.AJdbcPageDefinition;
import com.ajjpj.asysmon.servlet.performance.drilldown.ADrillDownPageDefinition;
import com.ajjpj.asysmon.servlet.threaddump.AThreadDumpPageDefinition;
import com.ajjpj.asysmon.servlet.trace.ATraceFilter;
import com.ajjpj.asysmon.servlet.trace.ATracePageDefinition;


/**
 * @author arno
 */
public class ADummyConfigFactory implements AConfigFactory {
    @Override public ASysMonConfig getConfig() {
        //TODO evaulate config files, make this stuff configurable
        final ASysMonConfigBuilder builder =
                new ASysMonConfigBuilder("demo", "1.0", "theInstance", "#ff8000")
                        .addEnvironmentMeasurer(new AEnvVarEnvironmentMeasurer())
                        .addEnvironmentMeasurer(new ASysPropEnvironmentMeasurer())
                        .addEnvironmentMeasurer(new ACpuEnvironmentMeasurer())
                        .addEnvironmentMeasurer(new AMemInfoEnvironmentMeasurer())
                        .addEnvironmentMeasurer(new AFileSystemsEnvironmentMeasurer())
                        .addEnvironmentMeasurer(new AOverviewEnvironmentMeasurer())
                        .addScalarMeasurer(new ASystemLoadMeasurer())
                        .addScalarMeasurer(AConnectionCounter.INSTANCE)
                        .addScalarMeasurer(new ACpuUtilizationMeasurer())
                        .addScalarMeasurer(new AProcSelfStatMeasurer())
                        .addScalarMeasurer(new AProcNetDevMeasurer())
                        .addScalarMeasurer(new AProcDiskstatsMeasurer())
                        .addPresentationMenuEntry("Context", new AEnvVarPageDefinition(), new AScalarPageDefinition())
                        .addPresentationMenuEntry("Trace", new ATracePageDefinition(ATraceFilter.ALL, 50), new ATracePageDefinition(ATraceFilter.HTTP, 30))
                        .addPresentationMenuEntry("Performance", new ADrillDownPageDefinition(), new AJdbcPageDefinition())
                        .addPresentationMenuEntry("Threads", new AThreadDumpPageDefinition("com.ajjpj")) //TODO make app package configurable
                        .addPresentationMenuEntry("Memory", new AMemGcPageDefinition());

        return builder.build();
    }
}
