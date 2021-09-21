package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class K8sStatusMetadata {

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("creationTimestamp")
    private String creationTimestamp;

    @JsonProperty("name")
    private String name;

    @JsonProperty("generateName")
    private String generateName;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("selfLink")
    private String selfLink;

    @JsonProperty("resourceVersion")
    private String resourceVersion;

    @JsonProperty("labels")
    private Map<String, String> labels;

    @JsonProperty("ownerReferences")
    private List<K8sOwnerReference> ownerReferences;

    @JsonProperty("annotations")
    private Map<String, String> annotations;

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(String creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

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
                ", name=" + name +
                '}';
    }
}
