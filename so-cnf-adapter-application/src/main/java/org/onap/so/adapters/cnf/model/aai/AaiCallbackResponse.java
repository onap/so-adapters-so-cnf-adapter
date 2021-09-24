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
