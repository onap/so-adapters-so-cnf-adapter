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

    @JsonProperty("error")
    private String errorMessage;

    public List<HealthCheckInstanceResponse> getInstanceResponse() {
        return instanceResponse;
    }

    public void setInstanceResponse(List<HealthCheckInstanceResponse> instanceResponse) {
        this.instanceResponse = instanceResponse;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "HealthCheckResponse{" +
                "instanceResponse=" + instanceResponse +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
