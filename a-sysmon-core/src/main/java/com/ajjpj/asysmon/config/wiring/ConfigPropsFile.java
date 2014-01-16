package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.config.log.ASysMonLoggerFactory;
import com.ajjpj.asysmon.util.AOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


/**
 * @author arno
 */
public class ConfigPropsFile {
    private final Properties props;
    private final ASysMonLogger log;

    public ConfigPropsFile(Properties props, ASysMonLoggerFactory log) {
        this.props = props;
        this.log = log.getLogger(ConfigPropsFile.class);
    }

    public <T> T get(String key, Class<T> type, Class<?>... paramTypes) {
        return get(key, AOption.<T>none(), type, paramTypes);
    }

    public <T> T get(String key, AOption<? extends T> defaultValue, Class<T> type, Class<?>... paramTypes) {
        final ConfigValueResolver r = new ConfigValueResolver(props, key, type, paramTypes);
        return (T) r.get(defaultValue);
    }

    public <T> List<T> getList(String key, Class<T> type) {
        final ConfigValueResolver r = new ConfigValueResolver(props, key, List.class, new Class[] {type});
        return (List<T>) r.get(AOption.some(Collections.emptyList()));
    }

    public List<String> getListRaw(String key) {
        final List<String> result = new ArrayList<String>();
        final String raw = props.getProperty(key);
        if(raw == null || raw.trim().isEmpty()) {
            return result;
        }

        final String[] split = raw.split(",");
        for(String s: split) {
            final String trimmed = s.trim();
            if(trimmed.isEmpty()) {
                log.warn("configuration error: empty list entry @ " + key + " - skipping");
            }
            else {
                result.add(trimmed);
            }
        }
        return result;
    }
}
