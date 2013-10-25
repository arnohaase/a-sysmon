package com.ajjpj.asysmon.datasink.jdbcreport;

/**
 * @author arno
 */
public class ATotalAndNum {
    private final long totalNanos;
    private final int num;

    public ATotalAndNum(long nanos) {
        this(nanos, 1);
    }

    private ATotalAndNum(long totalNanos, int num) {
        this.totalNanos = totalNanos;
        this.num = num;
    }

    public ATotalAndNum withNewValue(long nanos) {
        return new ATotalAndNum (totalNanos + nanos, num+1);
    }

    public long getTotalNanos() {
        return totalNanos;
    }

    public int getNum() {
        return num;
    }
}
