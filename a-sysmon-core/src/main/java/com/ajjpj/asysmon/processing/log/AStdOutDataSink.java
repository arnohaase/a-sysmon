package com.ajjpj.asysmon.processing.log;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.processing.ADataSink;


/**
 * This data sink prints measurement data to std out.
 *
 * @author arno
 */
public class AStdOutDataSink extends ALoggingDataSink {
    @Override protected void log(String s) {
        System.out.println(s);
    }
}
