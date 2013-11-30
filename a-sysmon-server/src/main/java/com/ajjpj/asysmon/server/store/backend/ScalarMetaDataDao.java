package com.ajjpj.asysmon.server.store.backend;

import com.ajjpj.asysmon.server.util.AOption;

import java.util.Map;


/**
 * @author arno
 */
public interface ScalarMetaDataDao {
    Map<String, ScalarMetaData> getAll();
    AOption<ScalarMetaData> get(String name);
    void store(ScalarMetaData scalarMetaData);
}
