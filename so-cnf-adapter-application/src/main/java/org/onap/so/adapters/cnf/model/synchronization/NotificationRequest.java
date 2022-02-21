package org.onap.so.adapters.cnf.model.synchronization;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class NotificationRequest {

    @JsonProperty("instance-id")
    private String instanceId;
    @JsonProperty("subscription-name")
    private String subscriptionName;
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public NotificationRequest() { }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "instanceId='" + instanceId + '\'' +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
