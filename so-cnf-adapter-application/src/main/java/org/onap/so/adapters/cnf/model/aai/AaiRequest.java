/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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
public class AaiRequest {

    @JsonProperty("instanceId")
    private String instanceId;
    @JsonProperty("cloudRegion")
    private String cloudRegion;
    @JsonProperty("cloudOwner")
    private String cloudOwner;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("callbackUrl")
    private String callbackUrl;
    @JsonProperty("genericVnfId")
    private String genericVnfId;
    @JsonProperty("vfModuleId")
    private String vfModuleId;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getGenericVnfId() {
        return genericVnfId;
    }

    public void setGenericVnfId(String genericVnfId) {
        this.genericVnfId = genericVnfId;
    }

    @Override
    public String toString() {
        return "AaiRequest{" +
                "instanceId='" + instanceId + '\'' +
                ", cloudRegion='" + cloudRegion + '\'' +
                ", cloudOwner='" + cloudOwner + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                ", genericVnfId='" + genericVnfId + '\'' +
                ", vfModuleId='" + vfModuleId + '\'' +
                '}';
    }
}
