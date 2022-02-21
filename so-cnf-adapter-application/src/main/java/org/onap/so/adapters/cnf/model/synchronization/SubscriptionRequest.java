package org.onap.so.adapters.cnf.model.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class SubscriptionRequest {

    @JsonProperty("name")
    private String callbackUrl;
    @JsonProperty("callback-url ")
    private String name;
    @JsonProperty("min-notify-interval")
    private int minNotifyInterval;
    @JsonProperty("metadata")
    private String metadata;

    public SubscriptionRequest() { }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinNotifyInterval() {
        return minNotifyInterval;
    }

    public void setMinNotifyInterval(int minNotifyInterval) {
        this.minNotifyInterval = minNotifyInterval;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "callbackUrl='" + callbackUrl + '\'' +
                ", name='" + name + '\'' +
                ", minNotifyInterval=" + minNotifyInterval +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
