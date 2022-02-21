package org.onap.so.adapters.cnf.model.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class RetrieveSubscriptionListResponse {

    @JsonProperty("subscriptions")
    private List<RetrieveSubscription> subscriptions;

    public RetrieveSubscriptionListResponse() { }

    public List<RetrieveSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<RetrieveSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public String toString() {
        return "RetrieveSubscriptionListResponse{" +
                "subscriptions=" + subscriptions +
                '}';
    }
}
