package org.onap.so.adapters.cnf.model.halthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class HealthCheckResponse {

    @JsonProperty("result")
    private List<HealthCheckInstanceResponse> instanceResponse;

    public List<HealthCheckInstanceResponse> getInstanceResponse() {
        return instanceResponse;
    }

    public void setInstanceResponse(List<HealthCheckInstanceResponse> instanceResponse) {
        this.instanceResponse = instanceResponse;
    }

    @Override
    public String toString() {
        return "HealthCheckResponse{" +
                "instanceResponse=" + instanceResponse +
                '}';
    }

}
