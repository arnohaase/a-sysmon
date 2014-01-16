package com.ajjpj.asysmon.config.log;

import com.ajjpj.asysmon.util.AStringFunction;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author arno
 */
public class AJavaUtilLogger extends ASysMonLogger {
    private final Logger log;

    public AJavaUtilLogger(Class<?> context) {
        this.log = Logger.getLogger(context.getName());
    }

    @Override public void debug(AStringFunction msg) {
        if(log.isLoggable(Level.FINE)) {
            log.fine(msg.apply());
        }
    }

    @Override public void info(String msg) {
        log.info(msg);
    }

    @Override public void warn(String msg) {
        log.warning(msg);
    }

    @Override public void warn(String msg, Exception exc) {
        log.log(Level.WARNING, msg, exc);
    }

    @Override public void error(String msg) {
        log.severe(msg);
    }

    @Override public void error(String msg, Exception exc) {
        log.log(Level.SEVERE, msg, exc);
    }
}
