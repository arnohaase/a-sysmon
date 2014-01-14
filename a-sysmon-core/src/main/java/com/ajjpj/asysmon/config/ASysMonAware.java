package com.ajjpj.asysmon.config;

import com.ajjpj.asysmon.ASysMonApi;

/**
 * If a measurers or data sink implements this interface and is registered with an A-SysMon instance, it will be
 *  injected that A-SysMon instance as part of the registration process.
 *
 * @author arno
 */
public interface ASysMonAware {
    void setASysMon(ASysMonApi sysMon);
}
