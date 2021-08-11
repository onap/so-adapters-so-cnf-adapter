package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties
class K8sRbInstanceRequest {

    @JsonProperty("labels")
    private Map<String, String> labels;

    @JsonProperty("cloud-region")
    private String cloudRegion;

    @JsonProperty("override-values")
    private Map<String, String> overrideValues;

    @JsonProperty("release-name")
    private String releaseName;

    @JsonProperty("rb-name")
    private String rbName;

    @JsonProperty("rb-version")
    private String rbVersion;

    @JsonProperty("profile-name")
    private String profileName;

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public Map<String, String> getOverrideValues() {
        return overrideValues;
    }

    public void setOverrideValues(Map<String, String> overrideValues) {
        this.overrideValues = overrideValues;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getRbName() {
        return rbName;
    }

    public void setRbName(String rbName) {
        this.rbName = rbName;
    }

    public String getRbVersion() {
        return rbVersion;
    }

    public void setRbVersion(String rbVersion) {
        this.rbVersion = rbVersion;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "K8sRbInstanceRequest{" +
                "labels=" + labels +
                ", cloudRegion='" + cloudRegion + '\'' +
                ", overrideValues=" + overrideValues +
                ", releaseName='" + releaseName + '\'' +
                ", rbName='" + rbName + '\'' +
                ", rbVersion='" + rbVersion + '\'' +
                ", profileName='" + profileName + '\'' +
                '}';
    }
}
