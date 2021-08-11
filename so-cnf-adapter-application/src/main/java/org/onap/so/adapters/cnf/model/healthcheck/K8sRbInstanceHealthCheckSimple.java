package org.onap.so.adapters.cnf.model.healthcheck;

import com.fasterxml.jackson.annotation.JsonProperty;

public class K8sRbInstanceHealthCheckSimple {

    @JsonProperty("healthcheck-id")
    private String id;

    @JsonProperty("status")
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "K8sRbInstanceHealthCheckSimple{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
