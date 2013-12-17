package com.ajjpj.asysmon.config.log;


import com.ajjpj.asysmon.util.AStringFunction;

/**
 * @author arno
 */
public interface ASysMonLogger {
    void debug(AStringFunction msg);
    void warn(String msg);
    void error(String s);
}
