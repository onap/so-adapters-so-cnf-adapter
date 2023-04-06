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

import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.AS_DEPLOYMENT_ITEM_INST_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KIND_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_CONFIG_FILE_PATH_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_KINDS_RESULT_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.NAMESPACE_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.RELEASE_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DAEMON_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DEPLOYMENT;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_JOB;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_POD;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_REPLICA_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_SERVICE;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_STATEFUL_SET;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubernetesRequestTimeOut;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesClient;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesClientProvider;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.kubernetes.client.openapi.ApiClient;


/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class MonitorHelmInstallStatusTask extends AbstractServiceTask {


    private static final int MAX_RETRIES = 10;
    private static final String RETRY_COUNTER_PARAM_NAME = "retryCounter";
    private static final String IS_RESOURCE_READY_PARAM_NAME = "isResourceReady";
    private static final Logger logger = LoggerFactory.getLogger(MonitorHelmInstallStatusTask.class);
    private final KubernetesClientProvider kubernetesClientProvider;
    private final KubernetesClient kubernetesClient;

    @Autowired
    protected MonitorHelmInstallStatusTask(final DatabaseServiceProvider databaseServiceProvider,
            final KubernetesClientProvider kubernetesClientProvider, final KubernetesClient kubernetesClient) {
        super(databaseServiceProvider);
        this.kubernetesClientProvider = kubernetesClientProvider;
        this.kubernetesClient = kubernetesClient;
    }

    public void updateJobStatus(final DelegateExecution execution) {
        logger.info("Executing updateJobStatus ");
        final String kind = (String) execution.getVariable(KIND_PARAM_NAME);
        final String asDeploymentItemInstId = (String) execution.getVariable(AS_DEPLOYMENT_ITEM_INST_ID_PARAM_NAME);

        addJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                "Checking if resource " + kind + " is ready for asDeploymentItemInstId: " + asDeploymentItemInstId);

        execution.setVariable(RETRY_COUNTER_PARAM_NAME, 0);

        logger.info("Finished updateJobStatus ...");
    }

    public void isResourceReady(final DelegateExecution execution) {
        logger.info("Executing isResourceReady ");
        final String kind = (String) execution.getVariable(KIND_PARAM_NAME);
        final String releaseName = (String) execution.getVariable(RELEASE_NAME_PARAM_NAME);
        final String namespace = (String) execution.getVariable(NAMESPACE_NAME_PARAM_NAME);
        final String kubeConfigFile = (String) execution.getVariable(KUBE_CONFIG_FILE_PATH_PARAM_NAME);
        final String labelSelector = "app.kubernetes.io/instance=" + releaseName;
        try {
            final ApiClient apiClient = kubernetesClientProvider.getApiClient(kubeConfigFile);
            boolean isReady = false;
            logger.debug("Will check if resource type: {} is ready using namespace: {}, labelSelector: {}", kind,
                    namespace, labelSelector);
            switch (kind) {
                case KIND_JOB:
                    isReady = kubernetesClient.isJobReady(apiClient, namespace, labelSelector);
                    break;
                case KIND_POD:
                    isReady = kubernetesClient.isPodReady(apiClient, namespace, labelSelector);
                    break;
                case KIND_SERVICE:
                    isReady = kubernetesClient.isServiceReady(apiClient, namespace, labelSelector);
                    break;
                case KIND_DEPLOYMENT:
                    isReady = kubernetesClient.isDeploymentReady(apiClient, namespace, labelSelector);
                    break;
                case KIND_REPLICA_SET:
                    isReady = kubernetesClient.isReplicaSetReady(apiClient, namespace, labelSelector);
                    break;
                case KIND_DAEMON_SET:
                    isReady = kubernetesClient.isDaemonSetReady(apiClient, namespace, labelSelector);
                    break;
                case KIND_STATEFUL_SET:
                    isReady = kubernetesClient.isStatefulSetReady(apiClient, namespace, labelSelector);
                    break;

                default:
                    logger.warn("Unknown resource type {} setting {} flag to true", kind, IS_RESOURCE_READY_PARAM_NAME);
                    isReady = true;
                    break;
            }

            logger.debug("isReady: {}", isReady);
            execution.setVariable(IS_RESOURCE_READY_PARAM_NAME, isReady);

        } catch (final KubernetesRequestTimeOut kubernetesRequestTimeOut) {
            final Integer counter = (Integer) execution.getVariable(RETRY_COUNTER_PARAM_NAME);
            if (counter > MAX_RETRIES) {
                final String message = "Retries max out for resource: " + kind;
                logger.error(message);
                abortOperation(execution, message);
            }
            logger.debug("Current retries counter: {} will increament and try again", counter);
            execution.setVariable(RETRY_COUNTER_PARAM_NAME, counter + 1);
            execution.setVariable(IS_RESOURCE_READY_PARAM_NAME, false);

        } catch (final Exception exception) {
            final String message = "Unable to preform status check for resource " + kind;
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished isResourceReady ...");

    }

    public void checkIfOperationWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfOperationWasSuccessful ");

        final String kind = (String) execution.getVariable(KIND_PARAM_NAME);

        @SuppressWarnings("unchecked")
        final Map<String, Boolean> kubeKindResult =
                (Map<String, Boolean>) execution.getVariable(KUBE_KINDS_RESULT_PARAM_NAME);

        final boolean isReady = (boolean) execution.getVariable(IS_RESOURCE_READY_PARAM_NAME);
        logger.debug("{} status {}", kind, isReady ? "Successful" : "failed");
        kubeKindResult.put(kind, isReady);

        execution.setVariable(KUBE_KINDS_RESULT_PARAM_NAME, kubeKindResult);

        if (!isReady) {
            final String message = "Status check failed for resource: {}" + kind;
            logger.error(message);
            abortOperation(execution, message);
        }

        final String asDeploymentItemInstId = (String) execution.getVariable(AS_DEPLOYMENT_ITEM_INST_ID_PARAM_NAME);
        addJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                "Resource " + kind + " is ready for asDeploymentItemInstId: " + asDeploymentItemInstId);
        logger.info("Finished checkIfOperationWasSuccessful ...");
    }

    public void timeOutLogFailue(final DelegateExecution execution) {
        logger.info("Executing timeOutLogFailue ");
        final String message = "Is Resource ready operation timed out";
        logger.error(message);
        abortOperation(execution, message);
        logger.info("Finished timeOutLogFailue ...");
    }

}
