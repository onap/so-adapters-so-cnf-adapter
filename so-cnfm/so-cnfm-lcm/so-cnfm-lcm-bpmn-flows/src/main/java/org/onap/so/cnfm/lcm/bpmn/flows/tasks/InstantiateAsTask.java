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
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.DEPLOYMENT_ITEM_INSTANTIATE_REQUESTS;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.INSTANTIATE_AS_REQUEST_PARAM_NAME;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.STARTED;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubeConfigFileNotFoundException;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.SdcPackageRequestFailureException;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPackageParser;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcPackageProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.service.KubConfigProvider;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.AsInfoModificationRequestDeploymentItems;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class InstantiateAsTask extends AbstractServiceTask {
    private static final String KUBE_CONFIG_FILE_PARAM_NAME = "kubeConfigFile";
    private static final String DEPLOY_ITEM_INST_ID_TO_HELM_FILE_MAPPING_PARAM_NAME =
            "asDeploymentItemInstIdToHelmFileMapping";
    private static final String IS_AS_INSTANTIATION_SUCCESSFUL_PARAM_NAME = "isAsInstantiationSuccessful";

    private static final Logger logger = LoggerFactory.getLogger(InstantiateAsTask.class);

    private final String csarDir;

    private final SdcPackageProvider sdcPackageProvider;

    private final SdcCsarPackageParser sdcParser;
    private final KubConfigProvider kubConfigProvider;

    @Autowired
    public InstantiateAsTask(final DatabaseServiceProvider databaseServiceProvider,
            final SdcPackageProvider sdcPackageProvider, final SdcCsarPackageParser sdcParser,
            final KubConfigProvider kubConfigProvider, @Value("${cnfm.csar.dir:/app/csar}") final String csarDir) {
        super(databaseServiceProvider);
        this.sdcPackageProvider = sdcPackageProvider;
        this.sdcParser = sdcParser;
        this.kubConfigProvider = kubConfigProvider;
        this.csarDir = csarDir;
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, STARTED, "Instantiate AS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, FINISHED, "Instantiate AS workflow process finished");
    }

    public void updateAsInstanceStatusToInstantiating(final DelegateExecution execution) {
        logger.info("Executing updateAsInstanceStatusToInstantiating");
        setJobStatus(execution, IN_PROGRESS, "Updating AsInst Status to " + State.INSTANTIATING);
        updateAsInstanceStatus(execution, State.INSTANTIATING);

        logger.info("Finished executing updateNsInstanceStatusToInstantiating  ...");
    }

    public void updateAsInstanceStatusToInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateAsInstanceStatusToInstantiated");

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        setJobStatus(execution, FINISHED, "Successfully " + State.INSTANTIATED + " AS: " + asInstId);

        updateAsInstanceStatus(execution, State.INSTANTIATED);
        logger.info("Finished executing updateAsInstanceStatusToInstantiated  ...");
    }

    public void downloadHelmPackagesFromSdc(final DelegateExecution execution) {
        logger.info("Executing downloadHelmPackages ...");
        setJobStatus(execution, IN_PROGRESS, "Downloading helm packages");
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);

        final AsInst asInst = getAsInst(execution);
        final Optional<byte[]> optional = getSdcResourcePackage(execution, asInst.getAsdId());

        if (optional.isEmpty()) {
            final String message = "Unable to find ASD package using asdId: " + asInst.getAsdId();
            logger.error(message);
            abortOperation(execution, message);
        }

        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);

        final File dir = mkdirIfnotExists(csarDir, asInstId);

        final Map<String, String> asDeploymentItemInstIdToHelmFileMapping = new HashMap<>();

        asDeploymentItems.forEach(asDeploymentItem -> {
            try (final ByteArrayInputStream stream = new ByteArrayInputStream(optional.get());
                    final ZipInputStream zipInputStream = new ZipInputStream(stream);) {

                final String artifactFilePath = asDeploymentItem.getArtifactFilePath();
                final String asDeploymentItemInstId = asDeploymentItem.getAsDeploymentItemInstId();
                try (final ByteArrayOutputStream helmByteArrayOutputStream =
                        sdcParser.getFileInZip(zipInputStream, artifactFilePath);) {

                    final Path artifactPath = Paths.get(artifactFilePath);
                    final Path path = dir.toPath().resolve(asDeploymentItem.getAsDeploymentItemInstId())
                            .resolve(artifactPath.getFileName());

                    if (!Files.exists(path.getParent())) {
                        final File parentDir = path.getParent().toFile();
                        logger.debug("Creating sub directories to download helm chart file {}", parentDir.toString());
                        parentDir.mkdirs();
                    }

                    if (Files.exists(path)) {
                        logger.debug("{} file already exists will remove it", path);
                        Files.delete(path);
                    }

                    try (final OutputStream outputStream = new FileOutputStream(path.toString())) {
                        helmByteArrayOutputStream.writeTo(outputStream);
                    }

                    asDeploymentItemInstIdToHelmFileMapping.put(asDeploymentItemInstId, path.toString());

                }
            } catch (final IOException ioException) {
                final String message = "Unexpected exception occured while processing CSAR " + asInst.getAsdId();
                logger.error(message, ioException);
                abortOperation(execution, message);
            } catch (final NoSuchElementException noSuchElementException) {
                final String message = "Unable to find artifact " + asDeploymentItem.getArtifactFilePath();
                logger.error(message, noSuchElementException);
                abortOperation(execution, message);
            } catch (final Exception exception) {
                final String message = "Unexpected exception occured while downloading helm packages";
                logger.error(message, exception);
                abortOperation(execution, message);
            }
        });

        logger.info("asDeploymentItemInstIdToHelmFileMapping: {}", asDeploymentItemInstIdToHelmFileMapping);
        execution.setVariable(DEPLOY_ITEM_INST_ID_TO_HELM_FILE_MAPPING_PARAM_NAME,
                asDeploymentItemInstIdToHelmFileMapping);

        logger.info("Finished executing downloadHelmPackages ...");
    }

    private Optional<byte[]> getSdcResourcePackage(final DelegateExecution execution, final String asdId) {
        try {
            return sdcPackageProvider.getSdcResourcePackage(asdId);
        } catch (final SdcPackageRequestFailureException exception) {
            final String message = "Unexpected exception occured while getting asd package using asdId: " + asdId;
            logger.error(message);
            abortOperation(execution, message);
        }
        return Optional.empty();
    }

    public void prepareInstantiateDeploymentItemRequests(final DelegateExecution execution) {
        logger.info("Executing prepareInstantiateDeploymentItemRequests ...");
        setJobStatus(execution, IN_PROGRESS, "Preparing InstantiateDeploymentItemRequest requests");

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final InstantiateAsRequest instantiateAsRequest =
                (InstantiateAsRequest) execution.getVariable(INSTANTIATE_AS_REQUEST_PARAM_NAME);

        @SuppressWarnings("unchecked")
        final Map<String, String> asDeploymentItemInstIdToHelmFileMapping =
                (Map<String, String>) execution.getVariable(DEPLOY_ITEM_INST_ID_TO_HELM_FILE_MAPPING_PARAM_NAME);

        final String kubeConfigFile = (String) execution.getVariable(KUBE_CONFIG_FILE_PARAM_NAME);

        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);

        final Set<InstantiateDeploymentItemRequest> requests = new TreeSet<>();
        final Map<String, Object> lifeCycleParamMap = instantiateAsRequest.getDeploymentItems().stream()
                .collect(Collectors.toMap(AsInfoModificationRequestDeploymentItems::getDeploymentItemsId,
                        AsInfoModificationRequestDeploymentItems::getLifecycleParameterKeyValues));
        asDeploymentItems.forEach(asDeploymentItem -> {

            final String asDeploymentItemInstId = asDeploymentItem.getAsDeploymentItemInstId();
            final String artifactFilePath = asDeploymentItemInstIdToHelmFileMapping.get(asDeploymentItemInstId);
            final String releaseName = asDeploymentItem.getReleaseName();

            if (Strings.isEmpty(artifactFilePath)) {
                final String message =
                        "Unable to find helm artifact for asDeploymentItemInstId: " + asDeploymentItemInstId;
                abortOperation(execution, message);
            }


            @SuppressWarnings("unchecked")
            final Map<String, String> lifeCycleParams =
                    (Map<String, String>) lifeCycleParamMap.get(asDeploymentItem.getItemId());

            final List<AsLifecycleParam> requiredParams = asDeploymentItem.getAsLifecycleParams();

            checkForLifecycleParametersAbort(execution, lifeCycleParams, requiredParams);
            requests.add(new InstantiateDeploymentItemRequest().asInstId(asInstId)
                    .asDeploymentItemInstId(asDeploymentItemInstId).asDeploymentItemName(asDeploymentItem.getName())
                    .helmArtifactFilePath(artifactFilePath).deploymentOrder(asDeploymentItem.getDeploymentOrder())
                    .kubeConfigFile(kubeConfigFile).lifeCycleParameters(lifeCycleParams).releaseName((releaseName)));

        });

        execution.setVariable(DEPLOYMENT_ITEM_INSTANTIATE_REQUESTS, requests);

        logger.info("Finished executing prepareInstantiateDeploymentItemRequests ...");

    }

    private void checkForLifecycleParametersAbort(final DelegateExecution execution,
            final Map<String, String> lifeCycleParams, final List<AsLifecycleParam> requiredParams) {
        if (!requiredParams.isEmpty()) {
            if (isNullOrEmptyMap(lifeCycleParams)) {
                abortOnLifecycleParams(execution, "no lifecycle parameters in request");
            }
            final Iterator<AsLifecycleParam> it = requiredParams.iterator();
            while (it.hasNext()) {
                final String next = it.next().getLifecycleParam();
                if (!lifeCycleParams.containsKey(next)) {
                    abortOnLifecycleParams(execution, "parameter missing: " + next);
                }
            }
        }
    }

    private void abortOnLifecycleParams(final DelegateExecution execution, final String reason) {
        final String message = "Lifecycle parameter error, " + reason;
        abortOperation(execution, message);
    }

    public void checkIfDeploymentItemsInstantiationWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfDeploymentItemsInstantiationWasSuccessful");

        @SuppressWarnings("unchecked")
        final Set<InstantiateDeploymentItemRequest> requests =
                (Set<InstantiateDeploymentItemRequest>) execution.getVariable(DEPLOYMENT_ITEM_INSTANTIATE_REQUESTS);

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);


        if (asDeploymentItems == null || asDeploymentItems.isEmpty()) {
            final String message = "Found empty asDeploymentItems";
            abortOperation(execution, message);
        }

        if (requests.size() != asDeploymentItems.size()) {
            final String message = "Missing asDeploymentItems. Request triggered has: " + requests.size()
                    + " asDeploymentItems but database has: " + asDeploymentItems.size();
            abortOperation(execution, message);
        }

        execution.setVariable(IS_AS_INSTANTIATION_SUCCESSFUL_PARAM_NAME, true);

        asDeploymentItems.stream().forEach(asDeploymentItem -> {
            logger.info("Checking if AsDeploymentItem {} was successfull Status: {}",
                    asDeploymentItem.getAsDeploymentItemInstId(), asDeploymentItem.getStatus());
            if (!State.INSTANTIATED.equals(asDeploymentItem.getStatus())) {
                logger.error("AsDeploymentItem : {} {} instantiation failed",
                        asDeploymentItem.getAsDeploymentItemInstId(), asDeploymentItem.getName());
                execution.setVariable(IS_AS_INSTANTIATION_SUCCESSFUL_PARAM_NAME, false);
            } else {
                cleanUpDeploymentItemDirectory(asInstId, asDeploymentItem.getAsDeploymentItemInstId());
            }
        });

        cleanUpInstanceIdDirectory(asInstId);
        logger.info("Finished executing checkIfDeploymentItemsInstantiationWasSuccessful  ...");
    }

    private void cleanUpDeploymentItemDirectory(final String asInstId, final String deploymentItemInstId) {
        logger.info("Executing Cleaning up Deployment Item Directory {}", deploymentItemInstId);
        final Path helmChartDirPath = Paths.get(csarDir, asInstId).resolve(deploymentItemInstId);
        if (Files.exists(helmChartDirPath)) {
            logger.debug("Will clean up the directory {}", helmChartDirPath);
            try {
                FileUtils.deleteDirectory(helmChartDirPath.toFile());
            } catch (final IOException e) {
                logger.debug("Error deleting the directory {}", helmChartDirPath);
            }
        }
    }

    private void cleanUpInstanceIdDirectory(final String asInstId) {
        logger.debug("Executing Cleaning up Instance Id Directory {}", asInstId);
        final Path dirPath = Paths.get(csarDir, asInstId);
        if (Files.exists(dirPath) && (dirPath.toFile().list().length == 0)) {
            logger.debug("Will clean up the instance id directory {}", dirPath);
            try {
                Files.delete(dirPath);
            } catch (final IOException e) {
                logger.debug("Error deleting the instance id directory {}", dirPath);
            }
        } else {
            logger.debug("Will not clean up the instance id directory. {} is not Empty", dirPath);
        }
    }

    public void checkifKubConfigFileAvailable(final DelegateExecution execution) {
        logger.info("Executing checkifKubConfigFileAvailable");
        try {
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

    public void logTimeOut(final DelegateExecution execution) {
        logger.error("Deployment items instantiation timedOut ...");
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final List<AsDeploymentItem> asDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInstId);
        if (asDeploymentItems != null) {
            asDeploymentItems.stream().forEach(asDeploymentItem -> {
                logger.info("Current status {} of asDeploymentItem: {}", asDeploymentItem.getStatus(),
                        asDeploymentItem.getName());
            });
        }
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        setJobStatusToError(execution, "Instantiate AS workflow process failed");
    }

    private File mkdirIfnotExists(final String parentDir, final String dirname) {
        final Path dirPath = Paths.get(parentDir, dirname);
        final File dir = dirPath.toFile();
        if (!dir.exists()) {
            logger.debug("Creating directory to download helm chart file {}", dir.toString());
            dir.mkdir();
        }
        return dir;
    }

    public static boolean isNullOrEmptyMap(final Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
}
