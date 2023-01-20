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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;
import static org.onap.so.cnfm.lcm.database.beans.utils.Utils.toIndentedString;

/**
 *
 * @author Gerard Nugent (gerard.nugent@est.tech)
 *
 */
@Entity
@Table(name = "AS_LIFECYCLE_PARAM")
public class AsLifecycleParam {

    @Id
    @Column(name = "AS_LCP_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer asLifecycleParamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AS_DEP_ITEM_INST_ID", nullable = false)
    private AsDeploymentItem asDeploymentItemInst;

    @Column(name = "LIFECYCLE_PARAM")
    private String lifecycleParam;

    public AsLifecycleParam() {

    }

    public void setAsLifecycleParamId(final Integer asLifecycleParamId) {
        this.asLifecycleParamId = asLifecycleParamId;
    }

    public Integer getAsLifecycleParamId() {
        return asLifecycleParamId;
    }

    public AsLifecycleParam asLifecycleParamId(final Integer asLifecycleParamId) {
        this.asLifecycleParamId = asLifecycleParamId;
        return this;
    }

    public AsDeploymentItem getAsDeploymentItemInst() {
        return asDeploymentItemInst;
    }

    public void setAsDeploymentItemInst(final AsDeploymentItem asDeploymentItemInst) {
        this.asDeploymentItemInst = asDeploymentItemInst;
    }

    public AsLifecycleParam asDeploymentItemInst(final AsDeploymentItem asDeploymentItemInst) {
        this.asDeploymentItemInst = asDeploymentItemInst;
        return this;
    }

    public String getLifecycleParam() {
        return lifecycleParam;
    }

    public void setLifecycleParam(final String lifecycleParam) {
        this.lifecycleParam = lifecycleParam;
    }

    public AsLifecycleParam asLifecycleParam(final String lifecycleParam) {
        this.lifecycleParam = lifecycleParam;
        return this;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class AslifecycleParam {\n");
        sb.append("    asLifecycleParamId: ").append(toIndentedString(asLifecycleParamId)).append("\n");
        sb.append("    asDeploymentItemInstId: ")
                .append(asDeploymentItemInst != null
                        ? toIndentedString(asDeploymentItemInst.getAsDeploymentItemInstId())
                        : null)
                .append("\n");
        sb.append("    lifecycleParam:  ").append(toIndentedString(lifecycleParam)).append("\n");
        sb.append("}");
        return sb.toString();
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final AsLifecycleParam that = (AsLifecycleParam) o;
        return Objects.equals(asLifecycleParamId, that.asLifecycleParamId)
                && Objects.equals(asDeploymentItemInst, that.asDeploymentItemInst)
                && Objects.equals(lifecycleParam, that.lifecycleParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asLifecycleParamId, asDeploymentItemInst, lifecycleParam);
    }


}
