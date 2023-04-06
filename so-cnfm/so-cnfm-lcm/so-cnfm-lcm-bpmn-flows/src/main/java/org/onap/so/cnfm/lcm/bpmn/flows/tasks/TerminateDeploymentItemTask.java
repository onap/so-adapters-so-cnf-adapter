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
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_CONFIG_FILE_PATH_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_KINDS_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_KINDS_RESULT_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.NAMESPACE_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.RELEASE_NAME_PARAM_NAME;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.helm.HelmClient;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesClientProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesResource;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Raviteja karumuri (raviteja.karumuri@est.tech)
 *
 */

@Component
public class TerminateDeploymentItemTask extends AbstractServiceTask {

    private static final Logger logger = LoggerFactory.getLogger(TerminateDeploymentItemTask.class);

    private static final String IS_DELETED_PARAM_NAME = "isDeleted";
    private static final String TERMINATE_REQUEST_PARAM_NAME = "request";

    private final AaiServiceProvider aaiServiceProvider;
    private final HelmClient helmClient;
    private final KubernetesClientProvider kubernetesClientProvider;

    @Autowired
    protected TerminateDeploymentItemTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider, final HelmClient helmClient,
            final KubernetesClientProvider kubernetesClientProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
        this.helmClient = helmClient;
        this.kubernetesClientProvider = kubernetesClientProvider;
    }

    public void checkIfDeploymentItemExistsInDb(final DelegateExecution execution) {
        logger.info("Executing checkIfDeploymentItemExistsInDb");
        final TerminateDeploymentItemRequest request =
                (TerminateDeploymentItemRequest) execution.getVariable(TERMINATE_REQUEST_PARAM_NAME);
        logger.info("Terminate request: {}", request);

        final String asDeploymentItemInstId = request.getAsDeploymentItemInstId();
        addJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                "Checking if Deployment item record exists in database for asDeploymentItemInstId: "
                        + asDeploymentItemInstId);

        if (!databaseServiceProvider.isAsDeploymentItemExists(request.getAsDeploymentItemInstId())) {
            abortOperation(execution, "Deployment Item does not exists in database for asDeploymentItemInstId: "
                    + asDeploymentItemInstId);
        }
        execution.setVariable(AS_DEPLOYMENT_ITEM_INST_ID_PARAM_NAME, request.getAsDeploymentItemInstId());
        execution.setVariable(KUBE_CONFIG_FILE_PATH_PARAM_NAME, request.getKubeConfigFile());
        logger.info("Finished executing checkIfDeploymentItemExistsInDb  ...");

    }

    public void unInstantiateHelmChart(final DelegateExecution execution) {
        logger.info("Executing unInstantiateHelmChart");

        final TerminateDeploymentItemRequest request =
                (TerminateDeploymentItemRequest) execution.getVariable(TERMINATE_REQUEST_PARAM_NAME);
        final String releaseName = request.getReleaseName();
        final String namespace = request.getNamespace();

        try {
            final Path kubeConfigFilePath = Paths.get(request.getKubeConfigFile());
            helmClient.unInstallHelmChart(namespace, releaseName, kubeConfigFilePath);
        } catch (final Exception exception) {
            final String message = "Failed to uninstall helm chart: " + " using kube-config file: "
                    + request.getKubeConfigFile() + "for reason: " + exception.getMessage();
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished executing unInstantiateHelmChart  ...");

    }


    public void updateDeploymentItemStatusToNotInstantiated(final DelegateExecution execution) {

        logger.info("Executing updateDeploymentItemStatusToNotInstantiated");
        final TerminateDeploymentItemRequest request =
                (TerminateDeploymentItemRequest) execution.getVariable(TERMINATE_REQUEST_PARAM_NAME);

        updateDeploymentItemStatus(execution, request.getAsDeploymentItemInstId(), State.NOT_INSTANTIATED);

        addJobStatus(execution, JobStatusEnum.FINISHED, "Successfully Terminated Deployment Item with "
                + "releaseName: " + request.getReleaseName() + " and will set status to " + State.NOT_INSTANTIATED);

        logger.info("Finished executing updateDeploymentItemStatusToNotInstantiated  ...");
    }

    public void getKubeKindsUsingManifestCommand(final DelegateExecution execution) {

        logger.info("Executing getKubeKindsFromReleaseHistory");

        final TerminateDeploymentItemRequest request =
                (TerminateDeploymentItemRequest) execution.getVariable(TERMINATE_REQUEST_PARAM_NAME);
        final String releaseName = request.getReleaseName();
        final String namespace = request.getNamespace();
        final Path kubeConfigFilePath = Paths.get(request.getKubeConfigFile());
        final Map<String, Boolean> kubeKindsMap = new HashMap<>();
        final List<String> kinds =
                helmClient.getKubeKindsUsingManifestCommand(namespace, releaseName, kubeConfigFilePath);
        if (kinds.isEmpty()) {
            abortOperation(execution,
                    "Unable to retrieve kinds from helm release history for releaseName: " + releaseName);
        }
        kinds.forEach(kind -> kubeKindsMap.put(kind, false));

        execution.setVariable(RELEASE_NAME_PARAM_NAME, releaseName);
        execution.setVariable(NAMESPACE_NAME_PARAM_NAME, namespace);
        execution.setVariable(KUBE_KINDS_RESULT_PARAM_NAME, kubeKindsMap);
        execution.setVariable(KUBE_KINDS_PARAM_NAME, kinds);
    }

    public void checkIfHelmUnInstallWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfHelmUnInstallWasSuccessful");

        @SuppressWarnings("unchecked")
        final Map<String, Boolean> kubeKindResult =
                (Map<String, Boolean>) execution.getVariable(KUBE_KINDS_RESULT_PARAM_NAME);

        execution.setVariable(IS_DELETED_PARAM_NAME, true);

        kubeKindResult.forEach((key, value) -> {
            logger.info("Checking if resource type {} was deleted Status: {}", key, value);

            if (Boolean.FALSE.equals(value)) {
                logger.error("resource type {} failed to delete", key);
                execution.setVariable(IS_DELETED_PARAM_NAME, false);
            }
        });

        final String kubeConfigFile = (String) execution.getVariable(KUBE_CONFIG_FILE_PATH_PARAM_NAME);
        kubernetesClientProvider.closeApiClient(kubeConfigFile);

        logger.info("Finished executing checkIfHelmUnInstallWasSuccessful  ...");

    }

    public void deleteK8ResourcesinAAI(final DelegateExecution execution) {
        logger.info("Executing deleteK8ResourcesinAai");
        final TerminateDeploymentItemRequest request =
                (TerminateDeploymentItemRequest) execution.getVariable(TERMINATE_REQUEST_PARAM_NAME);
        final List<KubernetesResource> resources =
                aaiServiceProvider.getK8sResources(request.getAsInstId(), request.getAsDeploymentItemInstId());
        final AsInst asInst = getAsInst(execution);
        resources.forEach(resource -> aaiServiceProvider.deleteK8SResource(resource.getId(), asInst.getCloudOwner(),
                asInst.getCloudRegion(), asInst.getTenantId()));
    }

    public void deleteVFModuleinAai(final DelegateExecution execution) {
        logger.info("Executing deleteVFModuleinAai");

        final TerminateDeploymentItemRequest request =
                (TerminateDeploymentItemRequest) execution.getVariable(TERMINATE_REQUEST_PARAM_NAME);
        aaiServiceProvider.deleteVfModule(request.getAsInstId(), request.getAsDeploymentItemInstId());
        logger.info("Delete VfModule in AAi is Done");

    }

}
