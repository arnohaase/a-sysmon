package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.asysmon.util.AList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author arno
 */
public interface AEnvironmentMeasurer {
    /**
     * @param data is a 'collectiong parameter', i.e. the method adds its own results to the existing collection.
     */
    void contributeMeasurements(EnvironmentCollector data) throws Exception;

    class EnvironmentCollector {
        public final Map<AList<String>, AEnvironmentData> data;

        public EnvironmentCollector(Map<AList<String>, AEnvironmentData> data) {
            this.data = data;
        }

        public void add(String value, String... key) {
            final AList<String> fullKey = AList.create(key);
            data.put(fullKey, new AEnvironmentData(fullKey, value));
        }
    }
}
