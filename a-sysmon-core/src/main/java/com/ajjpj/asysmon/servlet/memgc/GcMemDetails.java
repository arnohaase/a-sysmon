package com.ajjpj.asysmon.servlet.memgc;

/**
 * @author arno
 */
class GcMemDetails {
    public final String memKind;

    public final long usedBefore;
    public final long usedAfter;
    public final long committedBefore;
    public final long committedAfter;

    GcMemDetails(String memKind, long usedBefore, long usedAfter, long committedBefore, long committedAfter) {
        this.memKind = memKind;
        this.usedBefore = usedBefore;
        this.usedAfter = usedAfter;
        this.committedBefore = committedBefore;
        this.committedAfter = committedAfter;
    }
}
