package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class StatusCheckResponse {

    @JsonProperty("result")
    private List<StatusCheckInstanceResponse> instanceResponse;

    public List<StatusCheckInstanceResponse> getInstanceResponse() {
        return instanceResponse;
    }

    public void setInstanceResponse(List<StatusCheckInstanceResponse> instanceResponse) {
        this.instanceResponse = instanceResponse;
    }

    @Override
    public String toString() {
        return "StatusCheckResponse{" +
                "instanceResponse=" + instanceResponse +
                '}';
    }

}
