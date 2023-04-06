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
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.AS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_CONFIG_FILE_PATH_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_KINDS_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.KUBE_KINDS_RESULT_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.NAMESPACE_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.RELEASE_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DAEMON_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DEPLOYMENT;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_JOB;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_POD;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_REPLICA_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_SERVICE;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_STATEFUL_SET;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.K8SResource;
import org.onap.aai.domain.yang.VfModule;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.helm.HelmClient;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesClient;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesClientProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesResource;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
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
public class InstantiateDeploymentItemTask extends AbstractServiceTask {

    private static final Logger logger = LoggerFactory.getLogger(InstantiateDeploymentItemTask.class);

    private static final String KUBERNETES_RESOURCES_PARAM_NAME = "kubernetesResources";

    private static final String IS_SUCCESSFUL_PARAM_NAME = "isSuccessful";
    private static final String INSTANTIATE_REQUEST_PARAM_NAME = "request";

    private final AaiServiceProvider aaiServiceProvider;
    private final HelmClient helmClient;
    private final KubernetesClientProvider kubernetesClientProvider;
    private final KubernetesClient kubernetesClient;

    @Autowired
    protected InstantiateDeploymentItemTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider, final HelmClient helmClient,
            final KubernetesClientProvider kubernetesClientProvider, final KubernetesClient kubernetesClient) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
        this.helmClient = helmClient;
        this.kubernetesClientProvider = kubernetesClientProvider;
        this.kubernetesClient = kubernetesClient;
    }

    public void checkIfDeploymentItemExistsInDb(final DelegateExecution execution) {
        logger.info("Executing checkIfDeploymentItemExistsInDb");
        final InstantiateDeploymentItemRequest request =
                (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);
        logger.info("Instantiate request: {}", request);

        final String asDeploymentItemInstId = request.getAsDeploymentItemInstId();
        addJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                "Checking if Deployment item record exists in database for asDeploymentItemInstId: "
                        + asDeploymentItemInstId);

        if (!databaseServiceProvider.isAsDeploymentItemExists(request.getAsDeploymentItemInstId())) {
            abortOperation(execution, "Deployment Item does not exists in database for asDeploymentItemInstId: "
                    + asDeploymentItemInstId);
        }

        logger.info("Finished executing checkIfDeploymentItemExistsInDb  ...");

    }

    public void createVfModuleInAai(final DelegateExecution execution) {
        logger.info("Executing createVfModuleInAai  ...");
        try {
            final InstantiateDeploymentItemRequest request =
                    (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);

            setJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                    "Creating Vf Module Instance in AAI for " + request.getAsDeploymentItemInstId());

            final String asDeploymentItemInstId = request.getAsDeploymentItemInstId();

            final VfModule vfModule = new VfModule();
            vfModule.setVfModuleId(asDeploymentItemInstId);
            vfModule.setVfModuleName(request.getAsDeploymentItemName());
            vfModule.setIsBaseVfModule(true);
            vfModule.setAutomatedAssignment(true);
            vfModule.setOrchestrationStatus("Created");

            aaiServiceProvider.createVfModule(request.getAsInstId(), asDeploymentItemInstId, vfModule);

        } catch (final Exception exception) {
            final String message = "Unable to Create Vf Module Instance in AAI";
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished executing createVfModuleInAai  ...");

    }

    public void updateDeploymentItemStatusToInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateDeploymentItemStatusToInstantiated");
        final InstantiateDeploymentItemRequest request =
                (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);

        updateDeploymentItemStatus(execution, request.getAsDeploymentItemInstId(), State.INSTANTIATED);

        addJobStatus(execution, JobStatusEnum.FINISHED, "Successfully Instantiated Deployment Item: "
                + request.getAsDeploymentItemName() + " will set status to " + State.INSTANTIATED);

        logger.info("Finished executing updateDeploymentItemStatusToInstantiated  ...");

    }


    public void runHelmInstallDryRun(final DelegateExecution execution) {
        logger.info("Executing runHelmInstallDryRun");
        final InstantiateDeploymentItemRequest request =
                (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);

        final String releaseName = request.getReleaseName();
        final String namespace = request.getNamespace();

        try {
            final Path kubeConfigFilePath = Paths.get(request.getKubeConfigFile());
            final Path helmChartPath = Paths.get(request.getHelmArtifactFilePath());

            logger.debug("Running helm install with dry run flag");
            helmClient.runHelmChartInstallWithDryRunFlag(namespace, releaseName, kubeConfigFilePath, helmChartPath);
        } catch (final Exception exception) {
            final String message = "Unable to run helm install with dry run flag";
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished executing runHelmInstallDryRun  ...");

    }

    public void retrieveKubeKinds(final DelegateExecution execution) {
        logger.info("Executing retrieveKubeKinds");

        final InstantiateDeploymentItemRequest request =
                (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);

        try {
            final Path kubeConfigFilePath = Paths.get(request.getKubeConfigFile());
            final Path helmChartPath = Paths.get(request.getHelmArtifactFilePath());
            final String releaseName = request.getReleaseName();
            final String namespace = request.getNamespace();

            final List<String> kubeKinds =
                    helmClient.getKubeKinds(namespace, releaseName, kubeConfigFilePath, helmChartPath);

            if (kubeKinds.isEmpty()) {
                abortOperation(execution,
                        "Unable to retrieve kinds from chart / charts doesn't contains kinds: " + helmChartPath);
            }

            execution.setVariable(AS_DEPLOYMENT_ITEM_INST_ID_PARAM_NAME, request.getAsDeploymentItemInstId());
            execution.setVariable(KUBE_CONFIG_FILE_PATH_PARAM_NAME, request.getKubeConfigFile());
            execution.setVariable(KUBE_KINDS_PARAM_NAME, kubeKinds);

            final Map<String, Boolean> result = new HashMap<>();
            kubeKinds.forEach(kind -> result.put(kind, false));

            execution.setVariable(KUBE_KINDS_RESULT_PARAM_NAME, result);
        } catch (final BpmnError bpmnError) {
            throw bpmnError;
        } catch (final Exception exception) {
            final String message = "Unable to retrieve kube kinds";
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished executing retrieveKubeKinds  ...");

    }

    public void instantiateHelmChart(final DelegateExecution execution) {
        logger.info("Executing instantiateHelmChart");

        final InstantiateDeploymentItemRequest request =
                (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);

        final String namespace = request.getNamespace();
        final String releaseName = request.getReleaseName();

        execution.setVariable(RELEASE_NAME_PARAM_NAME, releaseName);
        execution.setVariable(NAMESPACE_PARAM_NAME, namespace);
        try {
            final Path kubeConfigFilePath = Paths.get(request.getKubeConfigFile());
            final Path helmChartPath = Paths.get(request.getHelmArtifactFilePath());
            final Map<String, String> lifeCycleParams = request.getLifeCycleParameters();

            helmClient.installHelmChart(namespace, releaseName, kubeConfigFilePath, helmChartPath, lifeCycleParams);
        } catch (final Exception exception) {
            final String message = "Unable to install helm chart: " + request.getHelmArtifactFilePath()
                    + " using kube-config file: " + request.getKubeConfigFile();
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished executing instantiateHelmChart  ...");

    }

    public void checkIfHelmInstallWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfHelmInstallWasSuccessful");

        @SuppressWarnings("unchecked")
        final Map<String, Boolean> kubeKindResult =
                (Map<String, Boolean>) execution.getVariable(KUBE_KINDS_RESULT_PARAM_NAME);

        execution.setVariable(IS_SUCCESSFUL_PARAM_NAME, true);

        kubeKindResult.entrySet().forEach(entry -> {
            logger.info("Checking if resource type {} was successfull Status: {}", entry.getKey(), entry.getValue());

            if (Boolean.FALSE.equals(entry.getValue())) {
                logger.error("resource type {} failed", entry.getKey());
                execution.setVariable(IS_SUCCESSFUL_PARAM_NAME, false);
            }
        });

        final String kubeConfigFile = (String) execution.getVariable(KUBE_CONFIG_FILE_PATH_PARAM_NAME);
        kubernetesClientProvider.closeApiClient(kubeConfigFile);

        logger.info("Finished executing checkIfHelmInstallWasSuccessful  ...");

    }

    public void retrieveKubernetesResources(final DelegateExecution execution) {
        logger.info("Executing retrieveKubernetesResources");
        final String namespace = (String) execution.getVariable(NAMESPACE_PARAM_NAME);
        final String releaseName = (String) execution.getVariable(RELEASE_NAME_PARAM_NAME);
        final String kubeConfigFile = (String) execution.getVariable(KUBE_CONFIG_FILE_PATH_PARAM_NAME);
        @SuppressWarnings("unchecked")
        final List<String> kubeKinds = (List<String>) execution.getVariable(KUBE_KINDS_PARAM_NAME);

        final String labelSelector = "app.kubernetes.io/instance=" + releaseName;

        if (kubeKinds != null) {
            final List<KubernetesResource> resources = new ArrayList<>();
            kubeKinds.forEach(kind -> {
                try {
                    final ApiClient apiClient = kubernetesClientProvider.getApiClient(kubeConfigFile);
                    logger.debug("Will check if resource type: {} is ready using labelSelector: {}", kind,
                            labelSelector);
                    switch (kind) {
                        case KIND_JOB:
                            resources.addAll(kubernetesClient.getJobResources(apiClient, namespace, labelSelector));
                            break;
                        case KIND_POD:
                            resources.addAll(kubernetesClient.getPodResources(apiClient, namespace, labelSelector));
                            break;
                        case KIND_SERVICE:
                            resources.addAll(kubernetesClient.getServiceResources(apiClient, namespace, labelSelector));
                            break;
                        case KIND_DEPLOYMENT:
                            resources.addAll(
                                    kubernetesClient.getDeploymentResources(apiClient, namespace, labelSelector));
                            break;
                        case KIND_REPLICA_SET:
                            resources.addAll(
                                    kubernetesClient.getReplicaSetResources(apiClient, namespace, labelSelector));
                            break;
                        case KIND_DAEMON_SET:
                            resources.addAll(
                                    kubernetesClient.getDaemonSetResources(apiClient, namespace, labelSelector));
                            break;
                        case KIND_STATEFUL_SET:
                            resources.addAll(
                                    kubernetesClient.getStatefulSetResources(apiClient, namespace, labelSelector));
                            break;
                        default:
                            logger.warn("Unknown resource type {} found skipping it ...", kind);
                            break;
                    }
                } catch (final Exception exception) {
                    final String message = "Unable to query kubernetes for resource " + kind;
                    logger.error(message, exception);
                    abortOperation(execution, message);
                }
            });
            logger.debug("Found resources : {}", resources);

            execution.setVariable(KUBERNETES_RESOURCES_PARAM_NAME, resources);

        }

        logger.info("Finished executing retrieveKubernetesResources  ...");

    }

    public void createK8sResourcesInAai(final DelegateExecution execution) {
        logger.info("Executing createK8sResourcesInAai");
        @SuppressWarnings("unchecked")
        final List<KubernetesResource> resources =
                (List<KubernetesResource>) execution.getVariable(KUBERNETES_RESOURCES_PARAM_NAME);
        if (resources == null) {
            abortOperation(execution, "resources cannot be null");
        }
        final InstantiateDeploymentItemRequest request =
                (InstantiateDeploymentItemRequest) execution.getVariable(INSTANTIATE_REQUEST_PARAM_NAME);

        setJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                "Creating K8s Resource in AAI for " + request.getAsDeploymentItemInstId());

        final AsInst asInst = getAsInst(execution);

        resources.forEach(kubernetesResource -> {
            try {
                final K8SResource k8sResource = new K8SResource();
                k8sResource.setId(kubernetesResource.getId());
                k8sResource.setName(kubernetesResource.getName());
                k8sResource.setGroup(kubernetesResource.getGroup());
                k8sResource.setVersion(kubernetesResource.getVersion());
                k8sResource.setKind(kubernetesResource.getKind());
                k8sResource.setNamespace(kubernetesResource.getNamespace());
                k8sResource.setDataSourceVersion(kubernetesResource.getResourceVersion());

                k8sResource.getLabels().addAll(kubernetesResource.getLabels());

                k8sResource.setDataOwner("so-cnfm");
                k8sResource.setDataSource("kubernetes");
                k8sResource.setSelflink("http://so-cnfm-lcm.onap:9888/query/");

                aaiServiceProvider.createK8sResource(kubernetesResource.getId(), asInst.getCloudOwner(),
                        asInst.getCloudRegion(), asInst.getTenantId(), k8sResource);

                aaiServiceProvider.connectK8sResourceToVfModule(kubernetesResource.getId(), asInst.getCloudOwner(),
                        asInst.getCloudRegion(), asInst.getTenantId(), request.getAsInstId(),
                        request.getAsDeploymentItemInstId());

                aaiServiceProvider.connectK8sResourceToGenericVnf(kubernetesResource.getId(), asInst.getCloudOwner(),
                        asInst.getCloudRegion(), asInst.getTenantId(), request.getAsInstId());


            } catch (final Exception exception) {
                final String message = "Unable to Create K8s Resource in AAI for " + kubernetesResource;
                logger.error(message, exception);
                abortOperation(execution, message);
            }
        });

        logger.info("Finished executing createK8sResourcesInAai  ...");

    }

    public void logTimeOut(final DelegateExecution execution) {
        logger.error("Checking helm install status timedOut ...");

        @SuppressWarnings("unchecked")
        final Map<String, Boolean> kubeKindResult =
                (Map<String, Boolean>) execution.getVariable(KUBE_KINDS_RESULT_PARAM_NAME);

        if (kubeKindResult != null) {
            kubeKindResult.entrySet().forEach(
                    entry -> logger.info("Current status {} of resource type: {}", entry.getValue(), entry.getKey()));
        }

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);
        if (asDeploymentItems != null) {
            asDeploymentItems.stream()
                    .forEach(asDeploymentItem -> logger.info("Current status {} of asDeploymentItem: {}",
                            asDeploymentItem.getStatus(), asDeploymentItem.getName()));
        }
    }

}
