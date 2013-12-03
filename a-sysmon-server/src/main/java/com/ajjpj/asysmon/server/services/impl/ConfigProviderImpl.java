package com.ajjpj.asysmon.server.services.impl;

import com.ajjpj.asysmon.server.services.ConfigData;
import com.ajjpj.asysmon.server.services.ConfigProvider;


/**
 * @author arno
 */
public class ConfigProviderImpl implements ConfigProvider {
    @Override public ConfigData getConfigData() {
        //TODO read from the database
        return new ConfigData(1000, 10_000, 1000, 4, 10, 10);
    }
}
