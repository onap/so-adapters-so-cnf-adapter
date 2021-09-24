/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
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

package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class K8sRbInstanceStatus {

    @JsonProperty("request")
    private MulticloudInstanceRequest request;

    @JsonProperty("resourceCount")
    private int resourceCount;

    @JsonProperty("ready")
    private boolean ready;

    @JsonProperty("resourcesStatus")
    private List<K8sRbInstanceResourceStatus> resourcesStatus;

    public MulticloudInstanceRequest getRequest() {
        return request;
    }

    public void setRequest(MulticloudInstanceRequest request) {
        this.request = request;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public List<K8sRbInstanceResourceStatus> getResourcesStatus() {
        return resourcesStatus;
    }

    public void setResourcesStatus(List<K8sRbInstanceResourceStatus> resourcesStatus) {
        this.resourcesStatus = resourcesStatus;
    }

    @Override
    public String toString() {
        return "K8sRbInstanceStatus{" +
                "request=" + request +
                ", resourceCount=" + resourceCount +
                ", ready=" + ready +
                ", resourcesStatus=" + resourcesStatus +
                '}';
    }
}
