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

package org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai;

import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.K8SResource;
import org.onap.aai.domain.yang.VfModule;
import org.onap.so.beans.nsmf.OrchestrationStatusEnum;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesResource;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface AaiServiceProvider {

    void createGenericVnfAndConnectServiceInstance(final String serviceInstanceId, final String vnfId,
            final GenericVnf genericVnf);

    void connectGenericVnfToTenant(final String vnfId, final String cloudOwner, final String cloudRegion,
            final String tenantId);

    Optional<GenericVnf> getGenericVnf(final String vnfId);

    void deleteGenericVnf(final String vnfId);

    void updateGenericVnf(final String vnfId, final GenericVnf vnf);

    void createVfModule(final String vnfId, final String vfModuleId, final VfModule vfModule);

    void createK8sResource(final String k8sResourceId, final String cloudOwner, final String cloudRegion,
            final String tenantId, K8SResource k8sResource);

    void connectK8sResourceToVfModule(final String k8sResourceId, final String cloudOwner, final String cloudRegion,
            final String tenantId, final String vnfId, final String vfModuleId);

    void connectK8sResourceToGenericVnf(final String k8sResourceId, final String cloudOwner, final String cloudRegion,
            final String tenantId, final String vnfId);

    List<KubernetesResource> getK8sResources(final String vnfId, final String vfModuleId);

    void deleteK8SResource(final String k8sResourceId, final String cloudOwner, final String cloudRegion,
            final String tenantId);

    void deleteVfModule(final String vnfId, final String vfModuleId);

    boolean updateGenericVnfStatus(final String vnfId, final OrchestrationStatusEnum status);
}
