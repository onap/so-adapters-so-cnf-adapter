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
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes;

import java.util.List;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubernetesRequestProcessingException;
import io.kubernetes.client.openapi.ApiClient;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface KubernetesClient extends KubernetesResourceStatusCheck {

    List<KubernetesResource> getJobResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

    List<KubernetesResource> getDeploymentResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

    List<KubernetesResource> getPodResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

    List<KubernetesResource> getServiceResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

    List<KubernetesResource> getReplicaSetResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

    List<KubernetesResource> getDaemonSetResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

    List<KubernetesResource> getStatefulSetResources(final ApiClient apiClient, final String namespace,
            final String labelSelector) throws KubernetesRequestProcessingException;

}
