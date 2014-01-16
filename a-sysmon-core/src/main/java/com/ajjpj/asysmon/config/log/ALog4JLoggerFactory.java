package com.ajjpj.asysmon.config.log;

/**
 * @author arno
 */
public class ALog4JLoggerFactory implements ASysMonLoggerFactory {
    public static final ALog4JLoggerFactory INSTANCE = new ALog4JLoggerFactory();

    @Override public ASysMonLogger getLogger(Class<?> context) {
        return new ALog4JLogger(context);
    }
}
