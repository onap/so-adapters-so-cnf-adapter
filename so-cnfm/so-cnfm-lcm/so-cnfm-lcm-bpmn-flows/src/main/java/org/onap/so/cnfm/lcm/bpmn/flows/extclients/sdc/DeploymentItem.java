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
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc;

import static org.onap.so.cnfm.lcm.database.beans.utils.Utils.toIndentedString;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class DeploymentItem implements Serializable {

    private static final long serialVersionUID = -1974244669409099225L;
    private String name;
    private String file;
    private String itemId;
    private String deploymentOrder;

    private List<String> lifecycleParameters;


    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DeploymentItem name(final String name) {
        this.name = name;
        return this;
    }


    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public DeploymentItem file(final String file) {
        this.file = file;
        return this;
    }


    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public DeploymentItem itemId(final String itemId) {
        this.itemId = itemId;
        return this;
    }

    public String getDeploymentOrder() {
        return deploymentOrder;
    }

    public void setDeploymentOrder(String deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
    }

    public DeploymentItem deploymentOrder(final String deploymentOrder) {
        this.deploymentOrder = deploymentOrder;
        return this;
    }


    public List<String> getLifecycleParameters() {
        return lifecycleParameters;
    }

    public void setLifecycleParameters(final List<String> lifecycleParameters) {
        this.lifecycleParameters = lifecycleParameters;
    }

    public DeploymentItem lifecycleParameters(final List<String> lifecycleParameters) {
        this.lifecycleParameters = lifecycleParameters;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, file, itemId, deploymentOrder, lifecycleParameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (obj instanceof DeploymentItem) {
            final DeploymentItem other = (DeploymentItem) obj;
            return Objects.equals(name, other.name) && Objects.equals(file, other.file)
                    && Objects.equals(itemId, other.itemId) && Objects.equals(deploymentOrder, other.deploymentOrder)
                    && Objects.equals(lifecycleParameters, other.lifecycleParameters);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class DeploymentItem {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    file: ").append(toIndentedString(file)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    deploymentOrder: ").append(toIndentedString(deploymentOrder)).append("\n");
        sb.append("    lifecycleParameters: ").append(toIndentedString(lifecycleParameters)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
