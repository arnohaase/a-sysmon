package com.ajjpj.asysmon.config.log;


import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class ALog4JLoggerFactory implements ASysMonLoggerFactory {
    public static final ALog4JLoggerFactory INSTANCE = new ALog4JLoggerFactory();

    static {
        Logger.getLogger(ALog4JLogger.class); // ensure that this does not load if Log4J is not on the classpath
    }

    @Override public ASysMonLogger getLogger(Class<?> context) {
        return new ALog4JLogger(context);
    }
}
