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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Entity
@Table(name = "AS_DEPLOYMENT_ITEM")
public class AsDeploymentItem {

    @Id
    @Column(name = "AS_DEP_ITEM_INST_ID", nullable = false)
    private String asDeploymentItemInstId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ITEM_ID")
    private String itemId;

    @Column(name = "DEPLOYMENT_ORDER")
    private Integer deploymentOrder;

    @Column(name = "ARTIFACT_FILE_PATH")
    private String artifactFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private State status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AS_INST_ID", nullable = false)
    private AsInst asInst;

    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;

    @Column(name = "LAST_UPDATE_TIME")
    private LocalDateTime lastUpdateTime;

    @Column(name = "RELEASE_NAME", nullable = false)
    private String releaseName;

    @OneToMany(mappedBy = "asDeploymentItemInst", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AsLifecycleParam> asLifecycleParams = new ArrayList<>();

    public AsDeploymentItem() {
        this.asDeploymentItemInstId = UUID.randomUUID().toString();
    }

    public String getAsDeploymentItemInstId() {
        return asDeploymentItemInstId;
    }

    public void setAsDeploymentItemInstId(final String asDeploymentItemInstId) {
        this.asDeploymentItemInstId = asDeploymentItemInstId;
    }

    public AsDeploymentItem asDeploymentItemInstId(final String asDeploymentItemInstId) {
        this.asDeploymentItemInstId = asDeploymentItemInstId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public AsDeploymentItem name(final String name) {
        this.name = name;
        return this;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public AsDeploymentItem itemId(final String itemId) {
        this.itemId = itemId;
        return this;
    }

    public Integer getDeploymentOrder() {
        return deploymentOrder;
    }

    public void setDeploymentOrder(final Integer deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
    }

    public AsDeploymentItem deploymentOrder(final Integer deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
        return this;
    }

    public String getArtifactFilePath() {
        return artifactFilePath;
    }

    public void setArtifactFilePath(final String artifactFilePath) {
        this.artifactFilePath = artifactFilePath;
    }

    public AsDeploymentItem artifactFilePath(final String artifactFilePath) {
        this.artifactFilePath = artifactFilePath;
        return this;
    }

    public State getStatus() {
        return status;
    }

    public void setStatus(final State status) {
        this.status = status;
    }

    public AsDeploymentItem status(final State status) {
        this.status = status;
        return this;
    }


    public AsInst getAsInst() {
        return asInst;
    }

    public void setAsInst(final AsInst asInst) {
        this.asInst = asInst;
    }

    public AsDeploymentItem asInst(final AsInst asInst) {
        this.asInst = asInst;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public AsDeploymentItem createTime(final LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public AsDeploymentItem lastUpdateTime(final LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(final String releaseName) {
        this.releaseName = releaseName;
    }

    public AsDeploymentItem releaseName(final String releaseName) {
        this.releaseName = releaseName;
        return this;
    }

    public List<AsLifecycleParam> getAsLifecycleParams() {
        return asLifecycleParams;
    }

    public void setAsLifecycleParams(final List<AsLifecycleParam> asLifecycleParams) {
        this.asLifecycleParams = asLifecycleParams;
    }

    public AsDeploymentItem asLifecycleParams(final AsLifecycleParam asLifecycleParam) {
        asLifecycleParam.asDeploymentItemInst(this);
        this.asLifecycleParams.add(asLifecycleParam);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final AsDeploymentItem that = (AsDeploymentItem) o;
        return Objects.equals(asDeploymentItemInstId, that.asDeploymentItemInstId) && Objects.equals(name, that.name)
                && Objects.equals(itemId, that.itemId) && Objects.equals(deploymentOrder, that.deploymentOrder)
                && Objects.equals(artifactFilePath, that.artifactFilePath)
                && (asInst == null ? that.asInst == null : that.asInst != null && Objects.equals(asInst, that.asInst))
                && Objects.equals(status, that.status) && Objects.equals(createTime, that.createTime)
                && Objects.equals(lastUpdateTime, that.lastUpdateTime) && Objects.equals(releaseName, that.releaseName)
                && Objects.equals(asLifecycleParams, that.asLifecycleParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asDeploymentItemInstId, name, itemId, deploymentOrder, artifactFilePath, status, asInst,
                createTime, lastUpdateTime, releaseName, asLifecycleParams);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class AsdeploymentItem {\n");
        sb.append("    asDeploymentItemInstId: ").append(toIndentedString(asDeploymentItemInstId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    deploymentOrder: ").append(toIndentedString(deploymentOrder)).append("\n");
        sb.append("    artifactFilePath: ").append(toIndentedString(artifactFilePath)).append("\n");
        sb.append("    asInst: ").append(asInst != null ? toIndentedString(asInst.getAsInstId()) : null).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    lastUpdateTime: ").append(toIndentedString(lastUpdateTime)).append("\n");
        sb.append("    releaseName: ").append(toIndentedString(releaseName)).append("\n");
        sb.append("    asLifecycleParams").append(toIndentedString(asLifecycleParams)).append("\n");
        sb.append("}");
        return sb.toString();
    }


}
