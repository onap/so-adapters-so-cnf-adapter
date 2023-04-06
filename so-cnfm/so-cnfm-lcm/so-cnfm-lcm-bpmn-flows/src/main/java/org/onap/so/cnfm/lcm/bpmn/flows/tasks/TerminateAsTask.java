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

import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.AS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.DEPLOYMENT_ITEM_TERMINATE_REQUESTS;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.STARTED;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.beans.nsmf.OrchestrationStatusEnum;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubeConfigFileNotFoundException;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.service.KubConfigProvider;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class TerminateAsTask extends AbstractServiceTask {
    private static final Logger logger = LoggerFactory.getLogger(TerminateAsTask.class);
    private final KubConfigProvider kubConfigProvider;
    private final AaiServiceProvider aaiServiceProvider;
    private static final String KUBE_CONFIG_FILE_PARAM_NAME = "kubeConfigFile";
    private static final String IS_AS_TERMINATION_SUCCESSFUL_PARAM_NAME = "isAsTerminationSuccessful";

    @Autowired
    protected TerminateAsTask(final DatabaseServiceProvider databaseServiceProvider,
            final KubConfigProvider kubConfigProvider, final AaiServiceProvider aaiServiceProvider) {
        super(databaseServiceProvider);
        this.kubConfigProvider = kubConfigProvider;
        this.aaiServiceProvider = aaiServiceProvider;
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, STARTED, "Terminate AS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, FINISHED, "Terminate AS workflow process finished");
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        setJobStatusToError(execution, "Terminate AS workflow process failed");
    }

    public void updateAsInstanceStatusToTerminating(final DelegateExecution execution) {
        logger.info("Executing updateAsInstanceStatusToTerminating");
        setJobStatus(execution, IN_PROGRESS, "Updating AsInst Status to " + State.TERMINATING);
        updateAsInstanceStatus(execution, State.TERMINATING);
        logger.info("Finished executing updateAsInstanceStatusToTerminating  ...");
    }

    public void updateAsInstanceStatusToNotInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateAsInstanceStatusToNotInstantiated");
        setJobStatus(execution, IN_PROGRESS, "Updating AsInst Status to " + State.NOT_INSTANTIATED);
        updateAsInstanceStatus(execution, State.NOT_INSTANTIATED);
        logger.info("Finished executing updateAsInstanceStatusToNotInstantiated  ...");
    }

    public void logTimeOut(final DelegateExecution execution) {
        logger.error("Deployment items Termination timedOut ...");
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);
        if (asDeploymentItems != null) {
            asDeploymentItems.stream()
                    .forEach(asDeploymentItem -> logger.info("Current status {} of terminating asDeploymentItem: {}",
                            asDeploymentItem.getStatus(), asDeploymentItem.getName()));
        }
    }

    public void checkifKubConfigFileAvailable(final DelegateExecution execution) {
        logger.info("Executing checkifKubConfigFileAvailable");
        try {
            setJobStatus(execution, IN_PROGRESS, "Checking if kubeconfig file is available or not");
            final AsInst asInst = getAsInst(execution);

            final Path kubeConfigFile = kubConfigProvider.getKubeConfigFile(asInst.getCloudOwner(),
                    asInst.getCloudRegion(), asInst.getTenantId());

            execution.setVariable(KUBE_CONFIG_FILE_PARAM_NAME, kubeConfigFile.toString());

        } catch (final KubeConfigFileNotFoundException exception) {
            final String message = "Unable to find kube-config file on filesystem";
            logger.error(message, exception);
            abortOperation(execution, message);

        }

        logger.info("Finished executing checkifKubConfigFileAvailable  ...");

    }

    public void prepareTerminateDeploymentItemRequests(final DelegateExecution execution) {
        logger.info("Executing prepareTerminateDeploymentItemRequests ...");
        setJobStatus(execution, IN_PROGRESS, "Preparing TerminateDeploymentItemRequest requests");

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final String kubeConfigFile = (String) execution.getVariable(KUBE_CONFIG_FILE_PARAM_NAME);

        final AsInst asInst = getAsInst(execution, asInstId);

        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);

        final Set<TerminateDeploymentItemRequest> requests = new TreeSet<>();

        asDeploymentItems.forEach(asDeploymentItem -> {

            final String asDeploymentItemInstId = asDeploymentItem.getAsDeploymentItemInstId();

            final TerminateDeploymentItemRequest terminatedeploymentitemrequest = new TerminateDeploymentItemRequest();
            terminatedeploymentitemrequest.setAsInstId(asInstId);
            terminatedeploymentitemrequest.setAsDeploymentItemInstId(asDeploymentItemInstId);
            terminatedeploymentitemrequest.setDeploymentOrder(asDeploymentItem.getDeploymentOrder());
            terminatedeploymentitemrequest.setKubeConfigFile(kubeConfigFile);
            terminatedeploymentitemrequest.setReleaseName(asDeploymentItem.getReleaseName());
            terminatedeploymentitemrequest.setNamespace(asInst.getNamespace());

            requests.add(terminatedeploymentitemrequest);

        });

        execution.setVariable(DEPLOYMENT_ITEM_TERMINATE_REQUESTS, requests);

        logger.info("Finished executing prepareTerminateDeploymentItemRequests ...");
    }

    public void checkIfDeploymentItemsTerminationWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfDeploymentItemsTerminationWasSuccessful");

        @SuppressWarnings("unchecked")
        final Set<TerminateDeploymentItemRequest> requests =
                (Set<TerminateDeploymentItemRequest>) execution.getVariable(DEPLOYMENT_ITEM_TERMINATE_REQUESTS);

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);


        if (asDeploymentItems == null || asDeploymentItems.isEmpty()) {
            final String message = "Found empty asDeploymentItems";
            abortOperation(execution, message);
        } else if (requests.size() != asDeploymentItems.size()) {
            final String message = "Missing asDeploymentItems. Request triggered has: " + requests.size()
                    + " asDeploymentItems but database has: " + asDeploymentItems.size();
            abortOperation(execution, message);
        } else {
            execution.setVariable(IS_AS_TERMINATION_SUCCESSFUL_PARAM_NAME, true);
            asDeploymentItems.forEach(asDeploymentItem -> {
                logger.info("Checking AsDeploymentItem {} termination status: {}",
                        asDeploymentItem.getAsDeploymentItemInstId(), asDeploymentItem.getStatus());
                if (!State.NOT_INSTANTIATED.equals(asDeploymentItem.getStatus())) {
                    logger.error("AsDeploymentItem : {} {} termination failed",
                            asDeploymentItem.getAsDeploymentItemInstId(), asDeploymentItem.getName());
                    execution.setVariable(IS_AS_TERMINATION_SUCCESSFUL_PARAM_NAME, false);
                }
            });
        }
        logger.info("Finished executing checkIfDeploymentItemsTerminationWasSuccessful  ...");
    }

    public void updateGenericVnfStatustoDeActivated(final DelegateExecution execution) {

        logger.debug("Executing updateGenericVnfStatustoDeActivated");
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final boolean result = aaiServiceProvider.updateGenericVnfStatus(asInstId, OrchestrationStatusEnum.DEACTIVATED);
        if (!result) {
            abortOperation(execution, "Failed to update GenericVnf status to Deactivated as there"
                    + "is no GenericVnf Found in AAI of ID: " + asInstId);
        }
        logger.info("Finished updating vnf status to Deactivated");
    }

}
