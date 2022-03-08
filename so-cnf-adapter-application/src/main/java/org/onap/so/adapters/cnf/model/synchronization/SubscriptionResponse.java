/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.cnf.model.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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
