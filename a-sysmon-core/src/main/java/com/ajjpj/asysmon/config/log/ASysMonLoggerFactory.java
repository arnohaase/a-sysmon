package com.ajjpj.asysmon.config.log;


/**
 * @author arno
 */
public interface ASysMonLoggerFactory {
    ASysMonLogger getLogger(Class<?> context);
}
