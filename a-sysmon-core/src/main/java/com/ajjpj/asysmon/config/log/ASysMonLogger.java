package com.ajjpj.asysmon.config.log;

import com.ajjpj.asysmon.config.ADefaultConfigFactory;
import com.ajjpj.asysmon.util.AStringFunction;


/**
 * @author arno
 */
public abstract class ASysMonLogger {
    public abstract void debug(AStringFunction msg);
    public abstract void info(String msg);
    public abstract void warn(String msg);
    public abstract void warn(String msg, Exception exc);
    public abstract void error(String msg);
    public abstract void error(String msg, Exception exc);

    /**
     * This method is the single point of access for loggers in A-SysMon
     */
    public static ASysMonLogger get(Class<?> context) {
        return ADefaultConfigFactory.getConfiguredLogger().getLogger(context);
    }
}
