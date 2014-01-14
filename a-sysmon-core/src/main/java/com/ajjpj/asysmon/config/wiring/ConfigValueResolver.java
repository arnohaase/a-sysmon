package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.util.AOption;
import com.ajjpj.asysmon.util.AUnchecker;

import java.util.Properties;


/**
 * @author arno
 */
class ConfigValueResolver {
    private static final ConfigTypeHandler[] typeHandlers = new ConfigTypeHandler[] {
            new HandlerForPrimitives(), new HandlerForList(), new HandlerForEnumsAndConstants(),
            new HandlerForBeans() // catch-all --> must be last
    };

    private final Properties props;
    private final ASysMonLogger log;

    private final String key;
    private final Class<?> type;
    private final Class<?>[] paramTypes;

    ConfigValueResolver(Properties props, ASysMonLogger log, String key, Class<?> type, Class<?>[] paramTypes) {
        this.props = props;
        this.log = log;

        this.key = key;
        this.type = type;
        this.paramTypes = paramTypes;
    }

    public Object get(String valueRaw) {
        final String value = valueRaw.trim();

        for(ConfigTypeHandler h: typeHandlers) {
            if(h.canHandle(type, paramTypes, value)) {
                try {
                    return h.handle(this, key, value, type, paramTypes);
                } catch (Exception exc) {
                    AUnchecker.throwUnchecked(exc);
                }
            }
        }

        throwConfigError("No Handler for type");
        return null; // for the compiler
    }

    public Object get(AOption<?> defaultValue) {
        final AOption<String> valueRaw = getRaw();
        if(! valueRaw.isDefined()) {
            if(defaultValue.isDefined()) {
                return defaultValue.get();
            }
            throwConfigError("Missing value, no default");
        }

        return get(valueRaw.get());
    }

    AOption<String> getRaw() {
        final AOption<String> aliasKey = child(null, null, "alias").getRawWithoutAlias();
        if(aliasKey.isDefined() && props.containsKey(aliasKey.get())) {
            return new ConfigValueResolver(props, log, aliasKey.get(), type, paramTypes).getRawWithoutAlias();
        }

        return getRawWithoutAlias();
    }

    private AOption<String> getRawWithoutAlias() {
        final String result = props.getProperty(key);
        if(result == null) {
            return AOption.none();
        }
        return AOption.some(result.trim());
    }

    ConfigValueResolver child(Class<?> type, Class<?>[] paramTypes, String... keySegments) {
        String childKey = key;
        for(String s: keySegments) {
            childKey += "." + s;
        }
        return new ConfigValueResolver(props, log, childKey, type, paramTypes);
    }

    void throwConfigError(String msg) {
        throwConfigError(msg, null);
    }

    void throwConfigError(String msg, Exception cause) { //TODO log the entire type hierarchy
        final StringBuilder paramT = new StringBuilder();
        if(paramTypes.length > 0) {
            paramT.append(" <");
            for(int i=0; i<paramTypes.length; i++) {
                if(i>0) {
                    paramT.append(", ");
                }
                paramT.append(paramTypes[i].getName());
            }
            paramT.append(">");
        }

        if(cause != null) {
            throw new IllegalArgumentException("config error @ " + key + " for type " + type.getName() + paramT + ": " + msg, cause);
        }
        else {
            throw new IllegalArgumentException("config error @ " + key + " for type " + type.getName() + paramT + ": " + msg);
        }
    }
}
