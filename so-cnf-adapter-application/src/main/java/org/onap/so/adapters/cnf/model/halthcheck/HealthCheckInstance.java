package org.onap.so.adapters.cnf.model.halthcheck;

public class HealthCheckInstance {
    private final String heatStackId;
    private final String healthCheckInstance;

    public HealthCheckInstance(String heatStackId, String healthCheckInstance) {
        this.heatStackId = heatStackId;
        this.healthCheckInstance = healthCheckInstance;
    }

    public String getHeatStackId() {
        return heatStackId;
    }

    public String getHealthCheckInstance() {
        return healthCheckInstance;
    }


    @Override
    public String toString() {
        return "HealthCheckInstance{" +
                "heatStackId='" + heatStackId + '\'' +
                ", healthCheckInstance='" + healthCheckInstance + '\'' +
                '}';
    }
}
