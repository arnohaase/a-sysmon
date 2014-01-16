package com.ajjpj.asysmon.testutil;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.config.log.ASysMonLoggerFactory;

/**
 * @author arno
 */
public class CountingLoggerFactory implements ASysMonLoggerFactory {
    public static final CountingLogger logger = new CountingLogger();

    @Override public ASysMonLogger getLogger(Class<?> context) {
        return logger;
    }
}
