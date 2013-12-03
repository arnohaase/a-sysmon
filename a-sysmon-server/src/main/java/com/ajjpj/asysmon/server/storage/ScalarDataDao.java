package com.ajjpj.asysmon.server.storage;

import com.ajjpj.asysmon.server.data.json.ScalarNode;

/**
 * @author arno
 */
public interface ScalarDataDao {
    void storeScalarData(ScalarNode scalarNode);
}
