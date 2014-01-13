package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.config.log.ASysMonLogger;

/**
 * @author arno
 */
public interface AConfigFactory {
    ASysMonConfig getConfig() throws Exception;
}
