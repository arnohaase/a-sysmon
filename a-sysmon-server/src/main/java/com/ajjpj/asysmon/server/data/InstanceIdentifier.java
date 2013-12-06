package com.ajjpj.asysmon.server.data;

/**
 * @author arno
 */
public class InstanceIdentifier {
    private final String applicationId;
    private final String instanceId;

    public InstanceIdentifier(String applicationId, String instanceId) {
        this.applicationId = applicationId;
        this.instanceId = instanceId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return "InstanceIdentifier{" +
                "applicationId='" + applicationId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceIdentifier that = (InstanceIdentifier) o;

        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
        return result;
    }
}
