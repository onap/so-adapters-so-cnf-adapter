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
package org.onap.so.cnfm.lcm.bpmn.flows.tasks;

import static org.onap.so.cnfm.lcm.database.beans.utils.Utils.toIndentedString;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class InstantiateDeploymentItemRequest implements Serializable, Comparable<InstantiateDeploymentItemRequest> {
    private static final long serialVersionUID = 6972854424933379019L;

    private String asInstId;
    private String asDeploymentItemInstId;
    private String asDeploymentItemName;
    private String helmArtifactFilePath;
    private String kubeConfigFile;
    private Integer deploymentOrder;
    private Map<String, String> lifeCycleParameters;
    private String releaseName;

    private static final Comparator<Integer> COMPARATOR = Comparator.nullsFirst(Integer::compare);

    public String getAsInstId() {
        return asInstId;
    }

    public void setAsInstId(final String asInstId) {
        this.asInstId = asInstId;
    }

    public InstantiateDeploymentItemRequest asInstId(final String asInstId) {
        this.asInstId = asInstId;
        return this;
    }

    public String getAsDeploymentItemInstId() {
        return asDeploymentItemInstId;
    }

    public void setAsDeploymentItemInstId(final String asDeploymentItemInstId) {
        this.asDeploymentItemInstId = asDeploymentItemInstId;
    }

    public InstantiateDeploymentItemRequest asDeploymentItemInstId(final String asDeploymentItemInstId) {
        this.asDeploymentItemInstId = asDeploymentItemInstId;
        return this;
    }

    public String getAsDeploymentItemName() {
        return asDeploymentItemName;
    }

    public void setAsDeploymentItemName(final String asDeploymentItemName) {
        this.asDeploymentItemName = asDeploymentItemName;
    }

    public InstantiateDeploymentItemRequest asDeploymentItemName(final String asDeploymentItemName) {
        this.asDeploymentItemName = asDeploymentItemName;
        return this;
    }

    public String getHelmArtifactFilePath() {
        return helmArtifactFilePath;
    }

    public void setHelmArtifactFilePath(final String helmArtifactFilePath) {
        this.helmArtifactFilePath = helmArtifactFilePath;
    }

    public InstantiateDeploymentItemRequest helmArtifactFilePath(final String helmArtifactFilePath) {
        this.helmArtifactFilePath = helmArtifactFilePath;
        return this;
    }

    public Integer getDeploymentOrder() {
        return deploymentOrder;
    }

    public void setDeploymentOrder(final Integer deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
    }

    public InstantiateDeploymentItemRequest deploymentOrder(final Integer deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
        return this;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(final String releaseName) {
        this.releaseName = releaseName;
    }

    public InstantiateDeploymentItemRequest releaseName(final String releaseName) {
        this.releaseName = releaseName;
        return this;
    }

    public String getKubeConfigFile() {
        return kubeConfigFile;
    }

    public void setKubeConfigFile(final String kubeConfigFile) {
        this.kubeConfigFile = kubeConfigFile;
    }

    public InstantiateDeploymentItemRequest kubeConfigFile(final String kubeConfigFile) {
        this.kubeConfigFile = kubeConfigFile;
        return this;
    }

    public Map<String, String> getLifeCycleParameters() {
        return lifeCycleParameters;
    }

    public void setLifeCycleParameters(final Map<String, String> lifeCycleParameters) {
        this.lifeCycleParameters = lifeCycleParameters;
    }

    public InstantiateDeploymentItemRequest lifeCycleParameters(final Map<String, String> lifeCycleParameters) {
        this.lifeCycleParameters = lifeCycleParameters;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asInstId, asDeploymentItemInstId, asDeploymentItemName, helmArtifactFilePath,
                deploymentOrder, kubeConfigFile, lifeCycleParameters, releaseName);
    }

    @Override
    public int compareTo(final InstantiateDeploymentItemRequest other) {
        return COMPARATOR.compare(this.getDeploymentOrder(), other.getDeploymentOrder());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof InstantiateDeploymentItemRequest) {
            final InstantiateDeploymentItemRequest other = (InstantiateDeploymentItemRequest) obj;
            return Objects.equals(asInstId, other.asInstId)
                    && Objects.equals(asDeploymentItemInstId, other.asDeploymentItemInstId)
                    && Objects.equals(asDeploymentItemName, other.asDeploymentItemName)
                    && Objects.equals(helmArtifactFilePath, other.helmArtifactFilePath)
                    && Objects.equals(deploymentOrder, other.deploymentOrder)
                    && Objects.equals(kubeConfigFile, other.kubeConfigFile)
                    && Objects.equals(lifeCycleParameters, other.lifeCycleParameters)
                    && Objects.equals(releaseName, other.releaseName);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class InstantiateDeploymentItemRequest {\n");
        sb.append("    asInstId: ").append(toIndentedString(asInstId)).append("\n");
        sb.append("    asDeploymentItemInstId: ").append(toIndentedString(asDeploymentItemInstId)).append("\n");
        sb.append("    asDeploymentItemName: ").append(toIndentedString(asDeploymentItemName)).append("\n");
        sb.append("    helmArtifactFilePath: ").append(toIndentedString(helmArtifactFilePath)).append("\n");
        sb.append("    deploymentOrder: ").append(toIndentedString(deploymentOrder)).append("\n");
        sb.append("    kubeConfigFile: ").append(toIndentedString(kubeConfigFile)).append("\n");
        sb.append("    LifeCycleParameters: ").append(toIndentedString(lifeCycleParameters)).append("\n");
        sb.append("    releaseName: ").append(toIndentedString(releaseName)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
