package com.ajjpj.asysmon.config.log;

import com.ajjpj.afoundation.function.AFunction0NoThrow;

import java.util.Date;


/**
 * @author arno
 */
public class AStdOutLogger extends ASysMonLogger {
    public volatile boolean isDebugEnabled = false;

    private final String context;

    public AStdOutLogger(Class<?> context) {
        String ctx = context.getName();
        if(ctx.length() > 40) {
            ctx = ctx.substring(ctx.length() - 40);
        }

        this.context = ctx + " ";
    }

    @Override public void debug(AFunction0NoThrow<String> msg) {
        if(isDebugEnabled) {
            log("DEBUG", msg.apply());
        }
    }

    private void log(String level, String msg) {
        System.out.println(Thread.currentThread().getName() + " " + new Date() + "  " + level + ": " + context + msg);
    }

    @Override public void info(String msg) {
        log("INFO ", msg);
    }

    @Override public void warn(String msg) {
        log("WARN ", msg);
    }

    @Override public void warn(String msg, Exception exc) {
        warn(msg);
        exc.printStackTrace(System.out);
    }

    @Override public void error(String msg) {
        log("ERROR", msg);
    }

    @Override public void error(Exception exc) {
        error("an exception occurred", exc);
    }

    @Override public void error(String msg, Exception exc) {
        error(msg);
        exc.printStackTrace(System.out);
    }
}
