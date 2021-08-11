package org.onap.so.adapters.cnf.model.statuscheck;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class K8sRbInstanceResourceStatus {

    @JsonProperty("name")
    private String name;

    @JsonProperty("GVK")
    private K8sRbInstanceGvk gvk;

    @JsonProperty("status")
    private Map<String, Object> status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public K8sRbInstanceGvk getGvk() {
        return gvk;
    }

    public void setGvk(K8sRbInstanceGvk gvk) {
        this.gvk = gvk;
    }

    public Map<String, Object> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Object> status) {
        this.status = status;
    }

}
