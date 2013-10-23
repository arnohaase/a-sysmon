package com.ajjpj.asysmon.processing.log;


/**
 * This data sink prints measurement data to std out.
 *
 * @author arno
 */
public class AStdErrDataSink extends ALoggingDataSink {
    @Override protected void log(String s) {
        System.err.println(s);
    }
}
