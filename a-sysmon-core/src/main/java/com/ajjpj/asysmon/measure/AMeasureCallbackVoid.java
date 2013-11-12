package com.ajjpj.asysmon.measure;

/**
 * @author arno
 */
public interface AMeasureCallbackVoid<E extends Exception> {
     void call(AWithParameters m) throws E;
}
