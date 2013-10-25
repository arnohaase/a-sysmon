package com.ajjpj.asysmon.datasink.log;


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
