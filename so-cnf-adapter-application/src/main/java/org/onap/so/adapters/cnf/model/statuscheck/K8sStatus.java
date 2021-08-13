package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class K8sStatus {

    @JsonProperty("metadata")
    private K8sStatusMetadata k8sStatusMetadata;

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
