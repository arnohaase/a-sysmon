package com.ajjpj.asysmon.config;


import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.measure.scalar.ASystemLoadMeasurer;
import com.ajjpj.asysmon.servlet.memgc.AMemGcPageDefinition;
import com.ajjpj.asysmon.servlet.performance.bottomup.AJdbcPageDefinition;
import com.ajjpj.asysmon.servlet.performance.drilldown.ADrillDownPageDefinition;
import com.ajjpj.asysmon.servlet.scalar.AScalarPageDefinition;
import com.ajjpj.asysmon.servlet.threaddump.AThreadDumpPageDefinition;
import com.ajjpj.asysmon.servlet.trace.ATraceFilter;
import com.ajjpj.asysmon.servlet.trace.ATracePageDefinition;

/**
 * This class evaluates the default configuration files, creating a config instance from their content.
 *
 * @author arno
 */
public class ADefaultConfigFactory {
    public ASysMonConfig getConfig() {
        //TODO evaulate config files, make this stuff configurable
        final ASysMonConfigBuilder builder =
                new ASysMonConfigBuilder("demo", "1.0", "theInstance", "#ff8000")
                        .addScalarMeasurer(new ASystemLoadMeasurer())
                        .addScalarMeasurer(AConnectionCounter.INSTANCE)
                        .addPresentationMenuEntry("Trace", new ATracePageDefinition(ATraceFilter.ALL, 50))
                        .addPresentationMenuEntry("Environment", new AScalarPageDefinition())
                        .addPresentationMenuEntry("Performance", new ADrillDownPageDefinition(), new AJdbcPageDefinition())
                        .addPresentationMenuEntry("Threads", new AThreadDumpPageDefinition("com.ajjpj")) //TODO make app package configurable
                        .addPresentationMenuEntry("Memory", new AMemGcPageDefinition());

        return builder.build();
    }
}
