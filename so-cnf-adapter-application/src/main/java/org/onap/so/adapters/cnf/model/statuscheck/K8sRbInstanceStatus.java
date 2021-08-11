package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class K8sRbInstanceStatus {

    @JsonProperty("request")
    private K8sRbInstanceRequest request;

    @JsonProperty("resourceCount")
    private int resourceCount;

    @JsonProperty("ready")
    private boolean ready;

    @JsonProperty("resourcesStatus")
    private List<K8sRbInstanceResourceStatus> resourcesStatus;

    public K8sRbInstanceRequest getRequest() {
        return request;
    }

    public void setRequest(K8sRbInstanceRequest request) {
        this.request = request;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public List<K8sRbInstanceResourceStatus> getResourcesStatus() {
        return resourcesStatus;
    }

    public void setResourcesStatus(List<K8sRbInstanceResourceStatus> resourcesStatus) {
        this.resourcesStatus = resourcesStatus;
    }

    @Override
    public String toString() {
        return "K8sRbInstanceStatus{" +
                "request=" + request +
                ", resourceCount=" + resourceCount +
                ", ready=" + ready +
                ", resourcesStatus=" + resourcesStatus +
                '}';
    }
}
