package org.onap.so.adapters.cnf.model.healthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
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
        return "StatusCheckResponse{" +
                "instanceResponse=" + instanceResponse +
                '}';
    }

}
