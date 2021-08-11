package org.onap.so.adapters.cnf.model.statuscheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class K8sRbInstanceStatus {

    @JsonProperty("request")
    private MulticloudInstanceRequest request;

    @JsonProperty("resourceCount")
    private int resourceCount;

    @JsonProperty("ready")
    private boolean ready;

    @JsonProperty("resourcesStatus")
    private List<K8sRbInstanceResourceStatus> resourcesStatus;

    public MulticloudInstanceRequest getRequest() {
        return request;
    }

    public void setRequest(MulticloudInstanceRequest request) {
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
