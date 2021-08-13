package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties
public class K8sStatusMetadata {

    @JsonProperty("metadata")
    private String namespace;

    @JsonProperty("metadata")
    private Map<String, String> labels;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "K8sStatusMetadata{" +
                "namespace='" + namespace + '\'' +
                ", labels=" + labels +
                '}';
    }
}
