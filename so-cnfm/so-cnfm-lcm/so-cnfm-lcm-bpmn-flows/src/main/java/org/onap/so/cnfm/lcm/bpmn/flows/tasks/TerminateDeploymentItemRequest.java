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
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 *
 */
public class TerminateDeploymentItemRequest implements Serializable, Comparable<TerminateDeploymentItemRequest> {

    private static final long serialVersionUID = 2953758424937589468L;
    private String asInstId;
    private String asDeploymentItemInstId;
    private String kubeConfigFile;
    private Integer deploymentOrder;
    private String releaseName;
    private String namespace;

    private static final Comparator<Integer> COMPARATOR = Comparator.nullsFirst(Integer::compare).reversed();

    public String getAsInstId() {
        return asInstId;
    }

    public void setAsInstId(final String asInstId) {
        this.asInstId = asInstId;
    }

    public String getAsDeploymentItemInstId() {
        return asDeploymentItemInstId;
    }

    public void setAsDeploymentItemInstId(final String asDeploymentItemInstId) {
        this.asDeploymentItemInstId = asDeploymentItemInstId;
    }

    public String getKubeConfigFile() {
        return kubeConfigFile;
    }

    public void setKubeConfigFile(final String kubeConfigFile) {
        this.kubeConfigFile = kubeConfigFile;
    }

    public Integer getDeploymentOrder() {
        return deploymentOrder;
    }

    public void setDeploymentOrder(final Integer deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(final String releaseName) {
        this.releaseName = releaseName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TerminateDeploymentItemRequest) {
            final TerminateDeploymentItemRequest that = (TerminateDeploymentItemRequest) obj;
            return Objects.equals(asInstId, that.asInstId)
                    && Objects.equals(asDeploymentItemInstId, that.asDeploymentItemInstId)
                    && Objects.equals(kubeConfigFile, that.kubeConfigFile)
                    && Objects.equals(deploymentOrder, that.deploymentOrder)
                    && Objects.equals(releaseName, that.releaseName) && Objects.equals(namespace, that.namespace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asInstId, asDeploymentItemInstId, kubeConfigFile, deploymentOrder, releaseName, namespace);
    }

    @Override
    public int compareTo(@NotNull final TerminateDeploymentItemRequest terminateDeploymentItemRequest) {
        return COMPARATOR.compare(this.getDeploymentOrder(), terminateDeploymentItemRequest.getDeploymentOrder());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class TerminateDeploymentItemRequest {\n");
        sb.append("    asInstId: ").append(toIndentedString(asInstId)).append("\n");
        sb.append("    asDeploymentItemInstId: ").append(toIndentedString(asDeploymentItemInstId)).append("\n");
        sb.append("    deploymentOrder: ").append(toIndentedString(deploymentOrder)).append("\n");
        sb.append("    kubeConfigFile: ").append(toIndentedString(kubeConfigFile)).append("\n");
        sb.append("    releaseName: ").append(toIndentedString(releaseName)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
