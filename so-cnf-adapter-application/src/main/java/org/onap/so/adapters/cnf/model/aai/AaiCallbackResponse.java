package org.onap.so.adapters.cnf.model.aai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class AaiCallbackResponse {

    @JsonProperty("status")
    private CompletionStatus completionStatus;

    @JsonProperty("statusMessage")
    private String message;

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(CompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AaiCallbackResponse{" +
                "completionStatus=" + completionStatus +
                ", message='" + message + '\'' +
                '}';
    }

    public enum CompletionStatus {
        COMPLETED, FAILED
    }
}
