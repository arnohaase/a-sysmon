package com.ajjpj.asysmon.server.data.json;

/**
 * @author arno
 */
public class CorrelationId {
    private String kind;
    private String ident;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    @Override
    public String toString() {
        return "CorrelationId{" +
                "kind='" + kind + '\'' +
                ", ident='" + ident + '\'' +
                '}';
    }
}
