package org.onap.so.adapters.cnf.model.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class SubscriptionListResponse {

    @JsonProperty("subscriptions")
    private List<SubscriptionResponse> subscriptions;

    public SubscriptionListResponse() { }

    public List<SubscriptionResponse> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionResponse> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public String toString() {
        return "SubscriptionListResponse{" +
                "subscriptions=" + subscriptions +
                '}';
    }
}
