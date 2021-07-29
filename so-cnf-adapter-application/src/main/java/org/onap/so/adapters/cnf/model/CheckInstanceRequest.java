package org.onap.so.adapters.cnf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.onap.so.adapters.cnf.model.halthcheck.InstanceRequest;

import java.util.List;

@JsonIgnoreProperties
public class CheckInstanceRequest {

    @JsonProperty("requestedInstances")
    private List<InstanceRequest> instances;

    @JsonProperty("callbackUrl")
    private String callbackUrl;

    public List<InstanceRequest> getInstances() {
        return instances;
    }

    public void setInstances(List<InstanceRequest> instances) {
        this.instances = instances;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String toString() {
        return "CheckInstanceRequest{" +
                "instances=" + instances +
                ", callbackUrl='" + callbackUrl + '\'' +
                '}';
    }
}
