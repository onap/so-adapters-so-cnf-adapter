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
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_AS_REQUEST_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_AS_RESPONSE_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.APPLICATION_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.APPLICATION_VERSION_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DEPLOYMENT_ITEMS_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DESCRIPTOR_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DESCRIPTOR_INVARIANT_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.PROVIDER_PARAM_NAME;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_OWNER_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_REGION_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.RESOURCE_ID_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.SERVICE_INSTANCE_ID_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.SERVICE_INSTANCE_NAME_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.TENANT_ID_PARAM_KEY;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.DeploymentItem;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPackageParser;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcPackageProvider;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.AsInstance.InstantiationStateEnum;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class CreateAsTask extends AbstractServiceTask {
    private static final String ASD_PROPERTIES_PARAM_NAME = "asdProperties";
    private static final String DOES_AS_PACKAGE_EXISTS_PARAM_NAME = "doesAsPackageExists";
    private static final String DOES_AS_INSTANCE_EXISTS_PARAM_NAME = "doesAsInstanceExists";
    private static final Logger logger = LoggerFactory.getLogger(CreateAsTask.class);

    private final AaiServiceProvider aaiServiceProvider;
    private final SdcPackageProvider sdcPackageProvider;
    private final SdcCsarPackageParser sdcParser;

    @Autowired
    public CreateAsTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider, final SdcPackageProvider sdcPackageProvider,
            final SdcCsarPackageParser sdcParser) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
        this.sdcPackageProvider = sdcPackageProvider;
        this.sdcParser = sdcParser;
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, JobStatusEnum.STARTED, "Create AS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, JobStatusEnum.FINISHED, "Create AS workflow process finished");
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        setJobStatusToError(execution, "Create AS workflow process failed");
    }

    public void getAsPackage(final DelegateExecution execution) {
        logger.info("Retrieving AS package from SDC ...");
        setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Retrieving AS package from SDC");

        final CreateAsRequest createAsRequest = (CreateAsRequest) execution.getVariable(CREATE_AS_REQUEST_PARAM_NAME);

        logger.info("Retrieving AS package from SDC using asdId: {}", createAsRequest.getAsdId());

        try {

            final Optional<byte[]> optional = sdcPackageProvider.getSdcResourcePackage(createAsRequest.getAsdId());

            if (optional.isPresent()) {
                logger.info("ASD Package exists for asdId {}", createAsRequest.getAsdId());

                final Map<String, Object> asdProperties = sdcParser.getAsdProperties(optional.get());
                logger.info("ASD Package properties fields {}", asdProperties);

                execution.setVariable(ASD_PROPERTIES_PARAM_NAME, asdProperties);
                execution.setVariable(DOES_AS_PACKAGE_EXISTS_PARAM_NAME, true);
            } else {

                final String message = "Unable to find ASD package using asdId: " + createAsRequest.getAsdId();
                logger.error(message);
                execution.setVariable(DOES_AS_PACKAGE_EXISTS_PARAM_NAME, false);
                abortOperation(execution, message);
            }
        } catch (final Exception failureException) {
            final String message =
                    "Unexpected exception occured while getting asd package using asdId: " + createAsRequest.getAsdId();
            logger.error(message, failureException);

            execution.setVariable(DOES_AS_PACKAGE_EXISTS_PARAM_NAME, false);
            execution.setVariable(AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME,
                    new ErrorDetails().title(message).detail(message));
        }

    }

    public void doesAsInstanceExistsInDb(final DelegateExecution execution) {
        logger.info("Executing doesAsInstanceExistsInDb  ...");

        setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Checking if AS Instance exists in database");

        final CreateAsRequest createAsRequest =
                (CreateAsRequest) execution.getVariables().get(CREATE_AS_REQUEST_PARAM_NAME);

        final boolean exists = databaseServiceProvider.isAsInstExists(createAsRequest.getAsInstanceName());
        logger.info("As Instance entry {} exists in database", exists ? "does" : "doesn't");
        execution.setVariable(DOES_AS_INSTANCE_EXISTS_PARAM_NAME, exists);

        if (exists) {
            execution.setVariable(AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, new ErrorDetails()
                    .detail("As Instance already exists in database for : " + createAsRequest.getAsInstanceName()));
        }

        logger.info("Finished executing doesAsInstanceExistsInDb  ...");

    }

    public void createAsInstanceInDb(final DelegateExecution execution) {
        try {
            logger.info("Executing createAsInstanceInDb  ...");

            setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Creating AS Instance entry in database");

            final CreateAsRequest createAsRequest =
                    (CreateAsRequest) execution.getVariable(CREATE_AS_REQUEST_PARAM_NAME);

            final Map<String, Object> additionalParams = createAsRequest.getAdditionalParams();

            if (additionalParams == null) {
                abortOperation(execution, "Missing 'additionalParams' mandatory field");
            }

            final String cloudOwner = getMandatoryValue(additionalParams, CLOUD_OWNER_PARAM_KEY, execution);
            final String cloudRegion = getMandatoryValue(additionalParams, CLOUD_REGION_PARAM_KEY, execution);
            final String tenantId = getMandatoryValue(additionalParams, TENANT_ID_PARAM_KEY, execution);
            final String resourceId = (String) additionalParams.get(RESOURCE_ID_KEY);

            final String serviceInstanceName =
                    getMandatoryValue(additionalParams, SERVICE_INSTANCE_NAME_PARAM_KEY, execution);
            final String serviceInstanceId =
                    getMandatoryValue(additionalParams, SERVICE_INSTANCE_ID_PARAM_KEY, execution);

            final String asInstId = getAsInstId(resourceId);
            execution.setVariable(AS_INSTANCE_ID_PARAM_NAME, asInstId);

            @SuppressWarnings("unchecked")
            final Map<String, Object> asdProperties =
                    (Map<String, Object>) execution.getVariable(ASD_PROPERTIES_PARAM_NAME);

            final AsInst asInst = new AsInst().asInstId(asInstId).name(createAsRequest.getAsInstanceName())
                    .asdId(createAsRequest.getAsdId())
                    .asPackageId(getParamValue(asdProperties, DESCRIPTOR_ID_PARAM_NAME))
                    .asdInvariantId(getParamValue(asdProperties, DESCRIPTOR_INVARIANT_ID_PARAM_NAME))
                    .asProvider(getParamValue(asdProperties, PROVIDER_PARAM_NAME))
                    .asApplicationName(getParamValue(asdProperties, APPLICATION_NAME_PARAM_NAME))
                    .asApplicationVersion(getParamValue(asdProperties, APPLICATION_VERSION_PARAM_NAME))
                    .description(createAsRequest.getAsInstanceDescription()).serviceInstanceId(serviceInstanceId)
                    .serviceInstanceName(serviceInstanceName).cloudOwner(cloudOwner).cloudRegion(cloudRegion)
                    .tenantId(tenantId).status(State.NOT_INSTANTIATED).statusUpdatedTime(LocalDateTime.now());

            @SuppressWarnings("unchecked")
            final List<DeploymentItem> deploymentItems =
                    (List<DeploymentItem>) asdProperties.get(DEPLOYMENT_ITEMS_PARAM_NAME);

            if (deploymentItems != null) {
                deploymentItems.forEach(item -> {
                    final AsDeploymentItem asDeploymentItem =
                            new AsDeploymentItem().itemId(item.getItemId()).name(item.getName())
                                    .deploymentOrder(item.getDeploymentOrder() != null
                                            ? Integer.parseInt(item.getDeploymentOrder())
                                            : null)
                                    .artifactFilePath(item.getFile()).status(State.NOT_INSTANTIATED)
                                    .createTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now())
                                    .releaseName(generateReleaseName(asInst, item));
                    final List<AsLifecycleParam> lifecycleParams = getLifeCycleParams(asDeploymentItem, item);
                    asDeploymentItem.setAsLifecycleParams(lifecycleParams);
                    asInst.asdeploymentItems(asDeploymentItem);
                });
            }

            databaseServiceProvider.saveAsInst(asInst);
            logger.info("Finished executing createAsInstanceInDb  ...");
        } catch (final Exception exception) {
            logger.error("Unable to create AsInst object in database", exception);
            throw exception;
        }

    }

    private List<AsLifecycleParam> getLifeCycleParams(final AsDeploymentItem asDeploymentItem,
            final DeploymentItem deploymentItem) {
        final List<AsLifecycleParam> asLifecycleParams = new ArrayList<>();
        if (deploymentItem.getLifecycleParameters() != null) {
            for (final String lifecycleParam : deploymentItem.getLifecycleParameters()) {
                asLifecycleParams.add(
                        new AsLifecycleParam().asDeploymentItemInst(asDeploymentItem).asLifecycleParam(lifecycleParam));
            }


        }
        return asLifecycleParams;
    }

    private String generateReleaseName(final AsInst asInst, final DeploymentItem item) {
        return String.join("-", Arrays.asList(asInst.getName(), item.getName(), item.getItemId())).toLowerCase()
                .replaceAll("[\\s\\_]", "-");
    }

    public void createGenericVnfInstanceInAai(final DelegateExecution execution) {
        logger.info("Executing createAsInstanceInAai  ...");
        try {
            setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Creating Generic Vnf Instance in AAI");

            final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
            final AsInst asInst = getAsInst(execution, asInstId);

            final GenericVnf genericVnf = new GenericVnf();
            genericVnf.setVnfId(asInstId);
            genericVnf.setVnfName(asInst.getName());
            genericVnf.setVnfType(asInst.getServiceInstanceName() + "/" + asInst.getName());
            genericVnf.setServiceId(asInst.getServiceInstanceId());
            genericVnf.setOperationalStatus("Created");
            genericVnf.setOrchestrationStatus("Created");
            genericVnf.setIsClosedLoopDisabled(false);
            aaiServiceProvider.createGenericVnfAndConnectServiceInstance(asInst.getServiceInstanceId(), asInstId,
                    genericVnf);

            aaiServiceProvider.connectGenericVnfToTenant(asInstId, asInst.getCloudOwner(), asInst.getCloudRegion(),
                    asInst.getTenantId());

        } catch (final Exception exception) {
            final String message = "Unable to Create Generic Vnf Instance in AAI";
            logger.error(message, exception);
            abortOperation(execution, message);
        }
        logger.info("Finished executing createNsInstanceInAai  ...");

    }

    public void setCreateAsResponse(final DelegateExecution execution) {
        logger.info("Executing setCreateAsResponse  ...");
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final Optional<AsInst> optional = databaseServiceProvider.getAsInst(asInstId);

        if (optional.isPresent()) {
            final AsInst asInst = optional.get();
            final AsInstance response =
                    new AsInstance().asInstanceid(asInst.getAsInstId()).asInstanceName(asInst.getName())
                            .asdId(asInst.getAsdId()).asInstanceDescription(asInst.getDescription())
                            .instantiationState(InstantiationStateEnum.fromValue(asInst.getStatus().toString()))
                            .asProvider(asInst.getAsProvider()).asApplicationName(asInst.getAsApplicationName())
                            .asApplicationVersion(asInst.getAsApplicationVersion());
            logger.info("Saving CreateNsResponse: {} in Execution ...", response);
            execution.setVariable(CREATE_AS_RESPONSE_PARAM_NAME, response);
        } else {
            final String message = "Unable to find AS Instance in datababse using id: " + asInstId;
            logger.error(message);
            abortOperation(execution, message);
        }

        logger.info("Finished executing setCreateNsResponse  ...");

    }

    private String getMandatoryValue(final Map<String, Object> additionalParams, final String key,
            final DelegateExecution execution) {
        final Object value = additionalParams.get(key);
        if (value == null) {
            abortOperation(execution, "Missing '" + key + "' mandatory field");
        }
        return value.toString();
    }

    private String getAsInstId(final String resourceId) {
        if ((resourceId != null) && !(resourceId.isBlank())) {
            logger.debug("Will use resourceId as asInstId: {}", resourceId);
            return resourceId;
        }
        final String asInstId = UUID.randomUUID().toString();
        logger.debug("Creating random UUID for asInstId: {}", asInstId);
        return asInstId;
    }

    private String getParamValue(final Map<String, Object> properties, final String key) {
        final Object object = properties.get(key);
        if (object != null) {
            return object.toString();
        }
        logger.warn("Unable to final property value for key {}", key);
        return null;
    }
}
