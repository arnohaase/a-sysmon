package com.ajjpj.asysmon.measure;

/**
 * @author arno
 */
public interface AMeasureCallback <R,E extends Exception> {
     R call(AWithParameters m) throws E;
}
