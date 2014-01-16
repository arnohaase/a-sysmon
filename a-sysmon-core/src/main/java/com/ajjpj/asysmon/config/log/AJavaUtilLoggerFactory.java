package com.ajjpj.asysmon.config.log;

/**
 * @author arno
 */
public class AJavaUtilLoggerFactory implements ASysMonLoggerFactory {
    public static final AJavaUtilLoggerFactory INSTANCE = new AJavaUtilLoggerFactory();

    @Override public ASysMonLogger getLogger(Class<?> context) {
        return new AJavaUtilLogger(context);
    }
}
