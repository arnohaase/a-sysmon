package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.config.log.ASysMonLogger;

import java.util.Properties;

/**
 * @author arno
 */
interface ConfigTypeHandler {
    boolean canHandle(Class<?> type, Class<?>[] paramTypes, String value);
    Object handle(ConfigValueResolver r, String key, String value, Class<?> type, Class<?>[] paramTypes) throws Exception;
}
