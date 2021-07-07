package org.onap.so.adapters.cnf.model.healthcheck;

public class HealthCheckInstance {
    private final String instanceId;
    private final String healthCheckInstance;

    public HealthCheckInstance(String instanceId, String healthCheckInstance) {
        this.instanceId = instanceId;
        this.healthCheckInstance = healthCheckInstance;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getHealthCheckInstance() {
        return healthCheckInstance;
    }

    @Override
    public String toString() {
        return "HealthCheckInstance{" +
                "instanceId='" + instanceId + '\'' +
                ", healthCheckInstance='" + healthCheckInstance + '\'' +
                '}';
    }
}
