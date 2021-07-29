package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class StatusCheckInstanceResponse {

    @JsonProperty("instanceId")
    private String instanceId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("status")
    private boolean status;

    public StatusCheckInstanceResponse() { }

    public StatusCheckInstanceResponse(String instanceId, String reason, boolean status) {
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusCheckInstanceResponse{" +
                "instanceId='" + instanceId + '\'' +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                '}';
    }
}
