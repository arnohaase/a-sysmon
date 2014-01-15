package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.appinfo.AApplicationInfoProvider;
import com.ajjpj.asysmon.appinfo.ADefaultApplicationInfoProvider;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.config.wiring.ConfigPropsFile;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.AFunction0;
import com.ajjpj.asysmon.util.AOption;
import com.ajjpj.asysmon.util.AUnchecker;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;


/**
 * This class evaluates the default configuration files, creating a config instance from their content.
 *
 * @author arno
 */
public class ADefaultConfigFactory implements AConfigFactory {
    public static final String DEFAULT_CONFIG_FILE = "asysmon-default.properties";
    public static final String CONFIG_FILE = "asysmon.properties";
    public static final String SYSPROP_PREFIX = "asysmon.";

    public static final String KEY_CONFIG_FACTORY = "config-factory";
    public static final String KEY_LOGGER = "logger";
    public static final String KEY_ENV_MEASURERS = "env-measurers";
    public static final String KEY_SCALAR_MEASURERS = "scalar-measurers";
    public static final String KEY_DATA_SINKS = "data-sinks";
    public static final String KEY_PRESENTATION_MENUS = "presentation-menus";

    public static final String KEY_AVERAGING_DELAY_FOR_SCALARS_MILLIS = "averaging-delay-for-scalars-millis";

    public static final String KEY_MEASUREMENT_TIMEOUT_NANOS = "measurement-timeout-nanos";
    public static final String KEY_MAX_NUM_MEASUREMENT_TIMEOUTS = "max-num-measurement-timeouts";

    public static final String KEY_DATA_SINK_TIMEOUT_NANOS = "data-sink-timeout-nanos";
    public static final String KEY_MAX_NUM_DATA_SINK_TIMEOUTS = "max-num-data-sink-timeouts";

    private static volatile ASysMonLogger configuredLogger;

    public static AConfigFactory getConfigFactory() {
        return AUnchecker.executeUnchecked(new AFunction0<AConfigFactory, Exception>() {
            @Override public AConfigFactory apply() throws Exception {
                final Properties propsRaw = getProperties();

                configuredLogger = getLogger(propsRaw);
                final ConfigPropsFile props = new ConfigPropsFile(propsRaw, getLogger(propsRaw));
                return props.get(KEY_CONFIG_FACTORY, AOption.some(new ADefaultConfigFactory()), AConfigFactory.class);
            }
        });
    }

    public static ASysMonLogger getConfiguredLogger() {
        if(configuredLogger == null) {
            getConfigFactory();
        }
        return configuredLogger;
    }

    private static ASysMonLogger getLogger(Properties props) {
        final String loggerClassName = props.getProperty(KEY_LOGGER);
        try {
            if(loggerClassName == null) {
                return ASysMonConfigBuilder.defaultLogger(); // avoid the warning log entry
            }

            return (ASysMonLogger) Class.forName(loggerClassName.trim()).newInstance();
        }
        catch(Exception exc) {
            final ASysMonLogger logger = ASysMonConfigBuilder.defaultLogger();
            logger.warn("exception creating logger based on config file entry '" + loggerClassName + "': " + exc);
            return logger;
        }
    }

    private static Properties getProperties() {
        try {
            final Properties result = new Properties();

            result.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE));

            final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
            if(in != null) {
                result.load(in);
            }

            for(String sysProp: System.getProperties().stringPropertyNames()) {
                if(! sysProp.startsWith(SYSPROP_PREFIX)) {
                    continue;
                }
                final String reducedKey = sysProp.substring(SYSPROP_PREFIX.length());
                result.setProperty(reducedKey, System.getProperty(sysProp));
            }

            return result;
        } catch (Exception e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    public ASysMonConfig getConfig() {
        final ConfigPropsFile props = new ConfigPropsFile(getProperties(), getConfiguredLogger());

        final AApplicationInfoProvider appInfo = AUnchecker.executeUnchecked(new AFunction0<AApplicationInfoProvider, Exception>() {
            @Override public AApplicationInfoProvider apply() throws Exception {
                return new ADefaultApplicationInfoProvider("demo", "version"); //TODO props.get("application-info", AApplicationInfoProvider.class);
            }
        });
//        final AApplicationInfoProvider appInfo = new ADefaultApplicationInfoProvider("demo", "version"); //TODO props.get("application-info", AApplicationInfoProvider.class);
        final ASysMonConfigBuilder builder = new ASysMonConfigBuilder(appInfo);
        builder.setLogger(getConfiguredLogger());
        builder.setAveragingDelayForScalarsMillis(props.get(KEY_AVERAGING_DELAY_FOR_SCALARS_MILLIS, Integer.TYPE));

        builder.setMeasurementTimeoutNanos(props.get(KEY_MEASUREMENT_TIMEOUT_NANOS, Long.TYPE));
        builder.setMaxNumMeasurementTimeouts(props.get(KEY_MAX_NUM_MEASUREMENT_TIMEOUTS, Integer.TYPE));

        builder.setDataSinkTimeoutNanos(props.get(KEY_DATA_SINK_TIMEOUT_NANOS, Long.TYPE));
        builder.setMaxNumDataSinkTimeouts(props.get(KEY_MAX_NUM_DATA_SINK_TIMEOUTS, Integer.TYPE));

        for(AEnvironmentMeasurer m: props.getList(KEY_ENV_MEASURERS, AEnvironmentMeasurer.class)) {
            builder.addEnvironmentMeasurer(m);
        }

        for(AScalarMeasurer m: props.getList(KEY_SCALAR_MEASURERS, AScalarMeasurer.class)) {
            builder.addScalarMeasurer(m);
        }

        for(ADataSink h: props.getList(KEY_DATA_SINKS, ADataSink.class)) {
            builder.addDataSink(h);
        }

        for(String menuEntryRaw: props.getListRaw(KEY_PRESENTATION_MENUS)) {
            final String menuEntry = menuEntryRaw.trim();

            final List<APresentationPageDefinition> pageDefs = props.getList(KEY_PRESENTATION_MENUS + "." + menuEntry, APresentationPageDefinition.class);
            builder.addPresentationMenuEntry(menuEntry, pageDefs);
        }

        return builder.build();
    }
}
