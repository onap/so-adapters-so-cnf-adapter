package org.onap.so.adapters.cnf.service.aai;

import java.util.Collection;

public class K8sResource {
    private String id;
    private String name;
    private String group;
    private String version;
    private String kind;
    private String namespace;
    private Collection<String> labels;
    private String k8sResourceSelfLink;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        this.labels = labels;
    }

    public String getK8sResourceSelfLink() {
        return k8sResourceSelfLink;
    }

    public void setK8sResourceSelfLink(String k8sResourceSelfLink) {
        this.k8sResourceSelfLink = k8sResourceSelfLink;
    }
}