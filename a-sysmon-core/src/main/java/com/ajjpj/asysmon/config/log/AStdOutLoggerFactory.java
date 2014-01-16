package com.ajjpj.asysmon.config.log;

/**
 * @author arno
 */
public class AStdOutLoggerFactory implements ASysMonLoggerFactory {
    public static final AStdOutLoggerFactory INSTANCE = new AStdOutLoggerFactory();

    @Override public ASysMonLogger getLogger(Class<?> context) {
        return new AStdOutLogger(context);
    }
}
