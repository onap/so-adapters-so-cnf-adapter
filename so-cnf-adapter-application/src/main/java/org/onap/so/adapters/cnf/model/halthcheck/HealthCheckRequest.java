package org.onap.so.adapters.cnf.model.halthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class HealthCheckRequest {

    @JsonProperty("instances")
    private List<InstanceRequest> instances;

    public List<InstanceRequest> getInstances() {
        return instances;
    }

    public void setInstances(List<InstanceRequest> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return "HealthCheckRequest{" +
                "instances=" + instances +
                '}';
    }
}
