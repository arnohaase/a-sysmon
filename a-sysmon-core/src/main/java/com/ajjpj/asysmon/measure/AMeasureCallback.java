package com.ajjpj.asysmon.measure;

/**
 * @author arno
 */
public interface AMeasureCallback <R,E extends Throwable> {
     R call(AWithParameters m) throws E;
}
