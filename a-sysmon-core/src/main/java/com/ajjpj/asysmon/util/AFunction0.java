package com.ajjpj.asysmon.util;

/**
 * @author arno
 */
public interface AFunction0<R, E extends Exception> {
    R apply() throws E;
}
