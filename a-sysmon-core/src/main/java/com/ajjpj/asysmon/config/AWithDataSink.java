package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.datasink.ADataSink;


/**
 * @author arno
 */
public interface AWithDataSink {
    ADataSink getDataSink();
}
