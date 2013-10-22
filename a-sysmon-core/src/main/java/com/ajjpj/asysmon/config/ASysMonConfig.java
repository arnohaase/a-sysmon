package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ATimer;

import java.util.List;

/**
 * @author arno
 */
public interface ASysMonConfig {
    ATimer getTimer();
    List<ADataSink> getHandlers();
}
