package org.onap.so.adapters.cnf.model.upgrade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class UpgradeRequest {

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
    private Collection<String> labels;

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

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
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
        UpgradeRequest that = (UpgradeRequest) o;
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
        return "UpgradeRequest{" +
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
}
