package org.onap.so.adapters.cnf.model.aai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class AaiCallbackResponse {

    @JsonProperty("completionStatus")
    private CompletionStatus completionStatus;

    @JsonProperty
    private String reason;

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(CompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "AaiCallbackResponse{" +
                "completionStatus=" + completionStatus +
                ", reason='" + reason + '\'' +
                '}';
    }

    public enum CompletionStatus {
        COMPLETED, FAILURE
    }
}
