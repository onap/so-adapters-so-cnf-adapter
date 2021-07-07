package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class StatusCheckResponse {

    @JsonProperty("result")
    private List<StatusCheckInstanceResponse> instanceResponse;

    @JsonProperty("errorMessage")
    private String errorMessage;

    public List<StatusCheckInstanceResponse> getInstanceResponse() {
        return instanceResponse;
    }

    public void setInstanceResponse(List<StatusCheckInstanceResponse> instanceResponse) {
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
        return "StatusCheckResponse{" +
                "instanceResponse=" + instanceResponse +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
