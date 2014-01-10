package com.ajjpj.asysmon.util;

/**
 * @author arno
 */
public interface AFunction1<P, R, E extends Exception> {
    R apply(P param) throws E;
}
