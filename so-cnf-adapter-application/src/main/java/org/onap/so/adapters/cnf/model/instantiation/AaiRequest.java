package org.onap.so.adapters.cnf.model.instantiation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class AaiRequest {

    private String instanceId;
    private String cloudRegion;
    private String cloudOwner;
    private String tenantId;
    private String callbackUrl;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String toString() {
        return "AaiRequest{" +
                "instanceId='" + instanceId + '\'' +
                ", cloudRegion='" + cloudRegion + '\'' +
                ", cloudOwner='" + cloudOwner + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                '}';
    }
}
