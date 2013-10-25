package com.ajjpj.asysmon.datasink.jdbcreport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author arno
 */
public class AJdbcStatementData {
    private final String ident;
    private final AtomicLong totalNanos;
    private final Map<String, AtomicReference<ATotalAndNum>> byEntryPoint = new ConcurrentHashMap<String, AtomicReference<ATotalAndNum>>();

    public AJdbcStatementData(String ident, long totalNanos) {
        this.ident = ident;
        this.totalNanos = new AtomicLong(totalNanos);
    }

    public String getIdent() {
        return ident;
    }

    public AtomicLong getTotalNanos() {
        return totalNanos;
    }

    public Map<String, AtomicReference<ATotalAndNum>> getByEntryPoint() {
        return byEntryPoint;
    }
}
