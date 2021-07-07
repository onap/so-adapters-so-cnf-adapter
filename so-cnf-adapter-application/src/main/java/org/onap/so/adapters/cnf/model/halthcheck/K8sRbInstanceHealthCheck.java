package org.onap.so.adapters.cnf.model.halthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class K8sRbInstanceHealthCheck {

    @JsonProperty("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "K8sRbInstanceHealthCheck{" +
                "status='" + status + '\'' +
                '}';
    }
}
