package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class K8sStatus {

    @JsonProperty("metadata")
    private K8sStatusMetadata k8sStatusMetadata;

    @JsonProperty("apiVersion")
    private String apiVersion;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("spec")
    private Map<String, Object> spec;

    @JsonProperty("status")
    private Map<String, Object> status;

    public Map<String, Object> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, Object> spec) {
        this.spec = spec;
    }

    public Map<String, Object> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Object> status) {
        this.status = status;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public K8sStatusMetadata getK8sStatusMetadata() {
        return k8sStatusMetadata;
    }

    public void setK8sStatusMetadata(K8sStatusMetadata k8sStatusMetadata) {
        this.k8sStatusMetadata = k8sStatusMetadata;
    }

    @Override
    public String toString() {
        return "K8sStatus{" +
                "k8sStatusMetadata=" + k8sStatusMetadata +
                '}';
    }
}
