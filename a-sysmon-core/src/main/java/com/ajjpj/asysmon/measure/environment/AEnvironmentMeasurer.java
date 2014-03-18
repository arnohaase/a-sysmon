package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.abase.collection.immutable.AList;
import com.ajjpj.asysmon.util.AShutdownable;

import java.util.List;


/**
 * @author arno
 */
public interface AEnvironmentMeasurer extends AShutdownable {
    /**
     * @param data is a 'collectiong parameter', i.e. the method adds its own results to the existing collection.
     */
    void contributeMeasurements(EnvironmentCollector data) throws Exception;

    class EnvironmentCollector {
        public final List<AEnvironmentData> data;

        public EnvironmentCollector(List<AEnvironmentData> data) {
            this.data = data;
        }

        public void add(String value, String... key) {
            final AList<String> fullKey = AList.create(key);
            data.add(new AEnvironmentData(fullKey, value));
        }
    }
}
