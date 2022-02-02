/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
 * Modifications Copyright (C) 2021 Orange.
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
package org.onap.so.adapters.cnf.model.upgrade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class InstanceUpgradeRequest {

    @JsonProperty("callbackUrl")
    private String callbackUrl;

    @JsonProperty("modelInvariantId")
    private String modelInvariantId;

    @JsonProperty("modelCustomizationId")
    private String modelCustomizationId;

    @JsonProperty("k8sRBProfileName")
    private String k8sRBProfileName;

    @JsonProperty("k8sRBInstanceStatusCheck")
    private boolean k8sRBInstanceStatusCheck;

    @JsonProperty("cloudRegionId")
    private String cloudRegionId;

    @JsonProperty("vfModuleUUID")
    private String vfModuleUUID;

    @JsonProperty("labels")
    private Map<String, String> labels;

    @JsonProperty("override-values")
    private Map<String, String> overrideValues;


    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    public String getK8sRBProfileName() {
        return k8sRBProfileName;
    }

    public void setK8sRBProfileName(String k8sRBProfileName) {
        this.k8sRBProfileName = k8sRBProfileName;
    }

    public boolean isK8sRBInstanceStatusCheck() {
        return k8sRBInstanceStatusCheck;
    }

    public void setK8sRBInstanceStatusCheck(boolean k8sRBInstanceStatusCheck) {
        this.k8sRBInstanceStatusCheck = k8sRBInstanceStatusCheck;
    }

    public String getCloudRegionId() {
        return cloudRegionId;
    }

    public void setCloudRegionId(String cloudRegionId) {
        this.cloudRegionId = cloudRegionId;
    }

    public String getVfModuleUUID() {
        return vfModuleUUID;
    }

    public void setVfModuleUUID(String vfModuleUUID) {
        this.vfModuleUUID = vfModuleUUID;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getOverrideValues() {
        return overrideValues;
    }

    public void setOverrideValues(Map<String, String> overrideValues) {
        this.overrideValues = overrideValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceUpgradeRequest that = (InstanceUpgradeRequest) o;
        return k8sRBInstanceStatusCheck == that.k8sRBInstanceStatusCheck &&
                Objects.equals(modelInvariantId, that.modelInvariantId) &&
                Objects.equals(modelCustomizationId, that.modelCustomizationId) &&
                Objects.equals(k8sRBProfileName, that.k8sRBProfileName) &&
                Objects.equals(cloudRegionId, that.cloudRegionId) &&
                Objects.equals(vfModuleUUID, that.vfModuleUUID) &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(overrideValues, that.overrideValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelInvariantId, modelCustomizationId, k8sRBProfileName, k8sRBInstanceStatusCheck, cloudRegionId, vfModuleUUID, labels, overrideValues);
    }

    @Override
    public String toString() {
        return "InstanceUpgradeRequest{" +
                "modelInvariantId='" + modelInvariantId + '\'' +
                ", modelCustomizationId='" + modelCustomizationId + '\'' +
                ", k8sRBProfileName='" + k8sRBProfileName + '\'' +
                ", k8sRBInstanceStatusCheck=" + k8sRBInstanceStatusCheck +
                ", cloudRegionId='" + cloudRegionId + '\'' +
                ", vfModuleUUID='" + vfModuleUUID + '\'' +
                ", labels=" + labels +
                ", overrideValues=" + overrideValues +
                '}';
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
