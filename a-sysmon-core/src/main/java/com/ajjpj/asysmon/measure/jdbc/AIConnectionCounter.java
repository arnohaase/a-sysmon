package com.ajjpj.asysmon.measure.jdbc;

/**
 * @author arno
 */
public interface AIConnectionCounter {
    void onOpenConnection(String qualifier);
    void onCloseConnection(String qualifier);

    void onActivateConnection(String qualifier);
    void onPassivateConnection(String qualifier);
}
