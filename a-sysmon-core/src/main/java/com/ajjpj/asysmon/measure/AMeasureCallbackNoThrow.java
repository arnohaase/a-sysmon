package com.ajjpj.asysmon.measure;

/**
 * @author arno
 */
public interface AMeasureCallbackNoThrow<R> extends AMeasureCallback<R, RuntimeException> {
    @Override R call(AWithParameters m);
}
