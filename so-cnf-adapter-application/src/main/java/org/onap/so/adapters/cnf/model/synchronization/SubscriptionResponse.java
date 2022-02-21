package org.onap.so.adapters.cnf.model.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class SubscriptionResponse {

    @JsonProperty("callback-url")
    private String callbackUrl;
    @JsonProperty("name")
    private String name;
    @JsonProperty("last-update-time")
    private Date lastUpdateTime;
    @JsonProperty("last-notify-time")
    private Date lastNotifyTime;
    @JsonProperty("last-notify-status")
    private int lastNotifyStatus;
    @JsonProperty("min-notify-interval")
    private int minNotifyInterval;
    @JsonProperty("metadata ")
    private String metadata;

    public SubscriptionResponse() { }

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

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getLastNotifyTime() {
        return lastNotifyTime;
    }

    public void setLastNotifyTime(Date lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }

    public int getLastNotifyStatus() {
        return lastNotifyStatus;
    }

    public void setLastNotifyStatus(int lastNotifyStatus) {
        this.lastNotifyStatus = lastNotifyStatus;
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
        return "SubscriptionResponse{" +
                "callbackUrl='" + callbackUrl + '\'' +
                ", name='" + name + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", lastNotifyTime=" + lastNotifyTime +
                ", lastNotifyStatus=" + lastNotifyStatus +
                ", minNotifyInterval=" + minNotifyInterval +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
