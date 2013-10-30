package com.ajjpj.asysmon.data;

/**
 * @author arno
 */
public class ACorrelationId {
    private final String qualifier;
    private final String id;

    /**
     * @param qualifier represents a 'kind' of correlation ID. This allows an application to differentiate between e.g.
     *                  SOA correlation IDs vs. correlation IDs of internal asynchronous cascades vs. 'conversation' IDs
     *                  in a web application
     * @param id is the actual identifier.
     */
    public ACorrelationId(String qualifier, String id) {
        this.qualifier = qualifier;
        this.id = id;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getId() {
        return id;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ACorrelationId that = (ACorrelationId) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (qualifier != null ? !qualifier.equals(that.qualifier) : that.qualifier != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = qualifier != null ? qualifier.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "ACorrelationId{" +
                "qualifier='" + qualifier + '\'' +
                ", id='" + id + '\'' +
                "} " + super.toString();
    }
}
