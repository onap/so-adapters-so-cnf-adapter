package org.onap.so.adapters.cnf.model.halthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class HealthCheckInstanceResponse {

    @JsonProperty("instanceId")
    private String instanceId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("status")
    private String status;

    public HealthCheckInstanceResponse() { }

    public HealthCheckInstanceResponse(String instanceId, String reason, String status) {
        this.instanceId = instanceId;
        this.reason = reason;
        this.status = status;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "HealthCheckInstanceResponse{" +
                "instanceId='" + instanceId + '\'' +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                '}';
    }
}
