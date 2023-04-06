/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cnfm.lcm.database.beans;

import static org.onap.so.cnfm.lcm.database.beans.utils.Utils.toIndentedString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Entity
@Table(name = "AS_INST")
public class AsInst {

    @Id
    @Column(name = "AS_INST_ID", nullable = false)
    private String asInstId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "AS_PACKAGE_ID")
    private String asPackageId;

    @Column(name = "ASD_ID", nullable = false)
    private String asdId;

    @Column(name = "ASD_INVARIANT_ID", nullable = false)
    private String asdInvariantId;

    @Column(name = "AS_PROVIDER", nullable = false)
    private String asProvider;

    @Column(name = "AS_APPLICATION_NAME", nullable = false)
    private String asApplicationName;

    @Column(name = "AS_APPLICATION_VERSION", nullable = false)
    private String asApplicationVersion;

    @Column(name = "SERVICE_INSTANCE_ID", nullable = false)
    private String serviceInstanceId;

    @Column(name = "SERVICE_INSTANCE_NAME", nullable = false)
    private String serviceInstanceName;

    @Column(name = "CLOUD_OWNER", nullable = false)
    private String cloudOwner;

    @Column(name = "CLOUD_REGION", nullable = false)
    private String cloudRegion;

    @Column(name = "TENANT_ID", nullable = false)
    private String tenantId;

    @Column(name = "NAME_SPACE", nullable = false)
    private String namespace;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private State status;

    @Column(name = "STATUS_UPDATED_TIME", nullable = false)
    private LocalDateTime statusUpdatedTime;

    @OneToMany(mappedBy = "asInst", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AsDeploymentItem> asdeploymentItems = new ArrayList<>();

    public AsInst() {
        this.asInstId = UUID.randomUUID().toString();
    }

    public String getAsInstId() {
        return asInstId;
    }

    public void setAsInstId(final String asInstId) {
        this.asInstId = asInstId;
    }

    public AsInst asInstId(final String asInstId) {
        this.asInstId = asInstId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public AsInst name(final String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public AsInst description(final String description) {
        this.description = description;
        return this;
    }

    public String getAsPackageId() {
        return asPackageId;
    }

    public void setAsPackageId(final String asPackageId) {
        this.asPackageId = asPackageId;
    }

    public AsInst asPackageId(final String asPackageId) {
        this.asPackageId = asPackageId;
        return this;
    }

    public String getAsdId() {
        return asdId;
    }

    public void setAsdId(final String asdId) {
        this.asdId = asdId;
    }

    public AsInst asdId(final String asdId) {
        this.asdId = asdId;
        return this;
    }

    public String getAsdInvariantId() {
        return asdInvariantId;
    }

    public void setAsdInvariantId(final String asdInvariantId) {
        this.asdInvariantId = asdInvariantId;
    }

    public AsInst asdInvariantId(final String nsdInvariantId) {
        this.asdInvariantId = nsdInvariantId;
        return this;
    }

    public String getAsProvider() {
        return asProvider;
    }

    public void setAsProvider(final String asProvider) {
        this.asProvider = asProvider;
    }

    public AsInst asProvider(final String asProvider) {
        this.asProvider = asProvider;
        return this;
    }

    public String getAsApplicationName() {
        return asApplicationName;
    }

    public void setAsApplicationName(final String asApplicationName) {
        this.asApplicationName = asApplicationName;
    }

    public AsInst asApplicationName(final String asApplicationName) {
        this.asApplicationName = asApplicationName;
        return this;
    }

    public String getAsApplicationVersion() {
        return asApplicationVersion;
    }

    public void setAsApplicationVersion(final String asApplicationVersion) {
        this.asApplicationVersion = asApplicationVersion;
    }

    public AsInst asApplicationVersion(final String asApplicationVersion) {
        this.asApplicationVersion = asApplicationVersion;
        return this;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(final String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public AsInst serviceInstanceId(final String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
        return this;
    }

    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(final String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    public AsInst serviceInstanceName(final String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
        return this;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(final String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public AsInst cloudOwner(final String cloudOwner) {
        this.cloudOwner = cloudOwner;
        return this;
    }

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(final String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public AsInst cloudRegion(final String cloudRegion) {
        this.cloudRegion = cloudRegion;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public AsInst tenantId(final String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public AsInst namespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public State getStatus() {
        return status;
    }

    public void setStatus(final State status) {
        this.status = status;
    }

    public AsInst status(final State status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(final LocalDateTime statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public AsInst statusUpdatedTime(final LocalDateTime statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
        return this;
    }

    public List<AsDeploymentItem> getAsdeploymentItems() {
        return asdeploymentItems;
    }

    public void setAsdeploymentItems(final List<AsDeploymentItem> asdeploymentItems) {
        this.asdeploymentItems = asdeploymentItems;
    }

    public AsInst asdeploymentItems(final AsDeploymentItem asdeploymentItem) {
        asdeploymentItem.asInst(this);
        this.asdeploymentItems.add(asdeploymentItem);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asInstId, name, description, asPackageId, asdId, asdInvariantId, asProvider,
                asApplicationName, asApplicationVersion, serviceInstanceId, serviceInstanceName, cloudOwner,
                cloudRegion, tenantId, namespace, status, statusUpdatedTime, asdeploymentItems);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (obj instanceof AsInst) {
            final AsInst other = (AsInst) obj;
            return Objects.equals(asInstId, other.asInstId) && Objects.equals(name, other.name)
                    && Objects.equals(description, other.description) && Objects.equals(asPackageId, other.asPackageId)
                    && Objects.equals(asdId, other.asdId) && Objects.equals(asdInvariantId, other.asdInvariantId)
                    && Objects.equals(asProvider, other.asProvider)
                    && Objects.equals(asApplicationName, other.asApplicationName)
                    && Objects.equals(asApplicationVersion, other.asApplicationVersion)
                    && Objects.equals(serviceInstanceId, other.serviceInstanceId)
                    && Objects.equals(serviceInstanceName, other.serviceInstanceName)
                    && Objects.equals(cloudOwner, other.cloudOwner) && Objects.equals(cloudRegion, other.cloudRegion)
                    && Objects.equals(tenantId, other.tenantId) && Objects.equals(namespace, other.namespace)
                    && Objects.equals(status, other.status)
                    && Objects.equals(statusUpdatedTime, other.statusUpdatedTime)
                    && Objects.equals(asdeploymentItems, other.asdeploymentItems);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class AsInst {\n");
        sb.append("    asInstId: ").append(toIndentedString(asInstId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    asPackageId: ").append(toIndentedString(asPackageId)).append("\n");
        sb.append("    asdId: ").append(toIndentedString(asdId)).append("\n");
        sb.append("    asProvider: ").append(toIndentedString(asProvider)).append("\n");
        sb.append("    asApplicationName: ").append(toIndentedString(asApplicationName)).append("\n");
        sb.append("    asApplicationVersion: ").append(toIndentedString(asApplicationVersion)).append("\n");
        sb.append("    serviceInstanceId: ").append(toIndentedString(serviceInstanceId)).append("\n");
        sb.append("    serviceInstanceName: ").append(toIndentedString(serviceInstanceName)).append("\n");
        sb.append("    cloudOwner: ").append(toIndentedString(cloudOwner)).append("\n");
        sb.append("    cloudRegion: ").append(toIndentedString(cloudRegion)).append("\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    statusUpdatedTime: ").append(toIndentedString(statusUpdatedTime)).append("\n");
        sb.append("    asdeploymentItems: ").append(toIndentedString(asdeploymentItems)).append("\n");

        sb.append("}");
        return sb.toString();
    }

}
