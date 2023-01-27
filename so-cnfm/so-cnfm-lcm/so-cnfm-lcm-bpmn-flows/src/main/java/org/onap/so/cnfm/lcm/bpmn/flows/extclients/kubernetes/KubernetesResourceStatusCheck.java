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

import io.kubernetes.client.openapi.ApiClient;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubernetesRequestProcessingException;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface KubernetesResourceStatusCheck {
    boolean isJobReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isPodReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isServiceReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isDeploymentReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isReplicaSetReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isDaemonSetReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isStatefulSetReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isServiceDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isPodDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isJobDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isDeploymentDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isReplicaSetDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isDaemonSetDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;

    boolean isStatefulSetDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException;
}
