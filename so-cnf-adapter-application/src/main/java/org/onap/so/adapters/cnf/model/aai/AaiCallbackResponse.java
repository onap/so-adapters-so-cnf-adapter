package org.onap.so.adapters.cnf.model.aai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class AaiCallbackResponse {

    @JsonProperty("status")
    private CompletionStatus status;

    @JsonProperty("statusMessage")
    private String statusMessage = "";

    public CompletionStatus getCompletionStatus() {
        return status;
    }

    public void setCompletionStatus(CompletionStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return statusMessage;
    }

    public void setMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return "AaiCallbackResponse{" +
                "status=" + status +
                ", statusMessage='" + statusMessage + '\'' +
                '}';
    }

    public enum CompletionStatus {
        COMPLETED, FAILED
    }
}
