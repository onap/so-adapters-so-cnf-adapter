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
package org.onap.so.cnfm.lcm.bpmn.flows.service;

import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.AS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_AS_REQUEST_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.INSTANTIATE_AS_REQUEST_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.JOB_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.OCC_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.TERMINATE_AS_REQUEST_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.CREATE_AS_WORKFLOW_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.DELETE_AS_WORKFLOW_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.INSTANTIATE_AS_WORKFLOW_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.TERMINATE_AS_WORKFLOW_NAME;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.ERROR;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.FINISHED_WITH_ERROR;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.slf4j.LoggerFactory.getLogger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.AsRequestProcessingException;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpType;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobAction;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class JobExecutorService {

    private static final Logger logger = getLogger(JobExecutorService.class);

    private static final ImmutableSet<JobStatusEnum> JOB_FINISHED_STATES =
            ImmutableSet.of(FINISHED, ERROR, FINISHED_WITH_ERROR);

    private static final int SLEEP_TIME_IN_SECONDS = 5;

    @Value("${so-cnfm-lcm.requesttimeout.timeoutInSeconds:300}")
    private int timeOutInSeconds;

    private final DatabaseServiceProvider databaseServiceProvider;
    private final WorkflowExecutorService workflowExecutorService;
    private final WorkflowQueryService workflowQueryService;
    private final Gson gson;

    @Autowired
    public JobExecutorService(final DatabaseServiceProvider databaseServiceProvider,
            final WorkflowExecutorService workflowExecutorService, final WorkflowQueryService workflowQueryService,
            final GsonProvider gsonProvider) {
        this.databaseServiceProvider = databaseServiceProvider;
        this.workflowExecutorService = workflowExecutorService;
        this.workflowQueryService = workflowQueryService;
        this.gson = gsonProvider.getGson();
    }

    public AsInstance runCreateAsJob(final CreateAsRequest createAsRequest) {
        logger.info("Starting 'Create AS' workflow job for request:\n{}", createAsRequest);
        final Job newJob = new Job().startTime(LocalDateTime.now()).jobType("AS").jobAction(JobAction.CREATE)
                .resourceId(createAsRequest.getAsdId()).resourceName(createAsRequest.getAsInstanceName())
                .status(JobStatusEnum.STARTING);
        databaseServiceProvider.addJob(newJob);

        logger.info("New job created in database :\n{}", newJob);

        workflowExecutorService.executeWorkflow(newJob.getJobId(), CREATE_AS_WORKFLOW_NAME,
                getVariables(newJob.getJobId(), createAsRequest));

        final ImmutablePair<String, JobStatusEnum> immutablePair =
                waitForJobToFinish(newJob.getJobId(), JOB_FINISHED_STATES);

        if (immutablePair.getRight() == null) {
            final String message = "Failed to create AS for request: \n" + createAsRequest;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }
        final JobStatusEnum finalJobStatus = immutablePair.getRight();
        final String processInstanceId = immutablePair.getLeft();

        if (!FINISHED.equals(finalJobStatus)) {

            final Optional<ErrorDetails> optional = workflowQueryService.getErrorDetails(processInstanceId);
            if (optional.isPresent()) {
                final ErrorDetails errorDetails = optional.get();
                final String message =
                        "Failed to create AS for request: \n" + createAsRequest + " due to \n" + errorDetails;
                logger.error(message);
                throw new AsRequestProcessingException(message, errorDetails);
            }

            final String message = "Received unexpected Job Status: " + finalJobStatus
                    + " Failed to Create AS for request: \n" + createAsRequest;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }

        logger.debug("Will query for CreateAsResponse using processInstanceId:{}", processInstanceId);
        final Optional<AsInstance> optional = workflowQueryService.getCreateNsResponse(processInstanceId);
        if (optional.isEmpty()) {
            final String message =
                    "Unable to find CreateAsReponse in Camunda History for process instance: " + processInstanceId;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }
        return optional.get();
    }

    public String runInstantiateAsJob(final String asInstanceId, final InstantiateAsRequest instantiateAsRequest) {
        final Job newJob = new Job().startTime(LocalDateTime.now()).jobType("AS").jobAction(JobAction.INSTANTIATE)
                .resourceId(asInstanceId).status(JobStatusEnum.STARTING);
        databaseServiceProvider.addJob(newJob);
        logger.info("New job created in database :\n{}", newJob);

        final LocalDateTime currentDateTime = LocalDateTime.now();
        final AsLcmOpOcc newAsLcmOpOcc = new AsLcmOpOcc().id(asInstanceId).operation(AsLcmOpType.INSTANTIATE)
                .operationState(OperationStateEnum.PROCESSING).stateEnteredTime(currentDateTime)
                .startTime(currentDateTime).asInst(getAsInst(asInstanceId)).isAutoInvocation(false)
                .isCancelPending(false).operationParams(gson.toJson(instantiateAsRequest));
        databaseServiceProvider.addAsLcmOpOcc(newAsLcmOpOcc);
        logger.info("New AsLcmOpOcc created in database :\n{}", newAsLcmOpOcc);

        workflowExecutorService.executeWorkflow(newJob.getJobId(), INSTANTIATE_AS_WORKFLOW_NAME,
                getVariables(asInstanceId, newJob.getJobId(), newAsLcmOpOcc.getId(), instantiateAsRequest));

        final ImmutableSet<JobStatusEnum> jobFinishedStates =
                ImmutableSet.of(FINISHED, ERROR, FINISHED_WITH_ERROR, IN_PROGRESS);
        final ImmutablePair<String, JobStatusEnum> immutablePair =
                waitForJobToFinish(newJob.getJobId(), jobFinishedStates);

        if (immutablePair.getRight() == null) {
            final String message = "Failed to Instantiate AS for request: \n" + instantiateAsRequest;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }

        final JobStatusEnum finalJobStatus = immutablePair.getRight();
        if (IN_PROGRESS.equals(finalJobStatus) || FINISHED.equals(finalJobStatus)) {
            logger.info("Instantiation Job status: {}", finalJobStatus);
            return newAsLcmOpOcc.getId();
        }

        final String message = "Received unexpected Job Status: " + finalJobStatus
                + " Failed to instantiate AS for request: \n" + instantiateAsRequest;
        logger.error(message);
        throw new AsRequestProcessingException(message);
    }

    public String runTerminateAsJob(final String asInstanceId, final TerminateAsRequest terminateAsRequest) {
        doInitialTerminateChecks(asInstanceId, terminateAsRequest);

        final Job newJob = new Job().startTime(LocalDateTime.now()).jobType("AS").jobAction(JobAction.TERMINATE)
                .resourceId(asInstanceId).status(JobStatusEnum.STARTING);
        databaseServiceProvider.addJob(newJob);
        logger.info("New job created in database :\n{}", newJob);

        final LocalDateTime currentDateTime = LocalDateTime.now();
        final AsLcmOpOcc newAsLcmOpOcc = new AsLcmOpOcc().id(asInstanceId).operation(AsLcmOpType.TERMINATE)
                .operationState(OperationStateEnum.PROCESSING).stateEnteredTime(currentDateTime)
                .startTime(currentDateTime).asInst(getAsInst(asInstanceId)).isAutoInvocation(false)
                .isCancelPending(false).operationParams(gson.toJson(terminateAsRequest));
        databaseServiceProvider.addAsLcmOpOcc(newAsLcmOpOcc);
        logger.info("New AsLcmOpOcc created in database :\n{}", newAsLcmOpOcc);

        workflowExecutorService.executeWorkflow(newJob.getJobId(), TERMINATE_AS_WORKFLOW_NAME,
                getVariables(asInstanceId, newJob.getJobId(), newAsLcmOpOcc.getId(), terminateAsRequest));

        final ImmutableSet<JobStatusEnum> jobFinishedStates =
                ImmutableSet.of(FINISHED, ERROR, FINISHED_WITH_ERROR, IN_PROGRESS);
        final ImmutablePair<String, JobStatusEnum> immutablePair =
                waitForJobToFinish(newJob.getJobId(), jobFinishedStates);

        if (immutablePair.getRight() == null) {
            final String message =
                    "Failed to Terminate AS with id: " + asInstanceId + " for request: \n" + terminateAsRequest;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }

        final JobStatusEnum finalJobStatus = immutablePair.getRight();

        if (IN_PROGRESS.equals(finalJobStatus) || FINISHED.equals(finalJobStatus)) {
            logger.info("Termination Job status: {}", finalJobStatus);
            return newAsLcmOpOcc.getId();
        }

        final String message = "Received unexpected Job Status: " + finalJobStatus + " Failed to Terminate AS with id: "
                + asInstanceId + " for request: \n" + terminateAsRequest;
        logger.error(message);
        throw new AsRequestProcessingException(message);
    }

    public void runDeleteAsJob(final String asInstanceId) {
        final Job newJob = new Job().startTime(LocalDateTime.now()).jobType("AS").jobAction(JobAction.DELETE)
                .resourceId(asInstanceId).status(JobStatusEnum.STARTING);
        databaseServiceProvider.addJob(newJob);
        logger.info("New job created in database :\n{}", newJob);

        workflowExecutorService.executeWorkflow(newJob.getJobId(), DELETE_AS_WORKFLOW_NAME,
                getVariables(asInstanceId, newJob.getJobId()));

        final ImmutablePair<String, JobStatusEnum> immutablePair =
                waitForJobToFinish(newJob.getJobId(), JOB_FINISHED_STATES);

        if (immutablePair.getRight() == null) {
            final String message = "Failed to Delete AS with id: " + asInstanceId;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }

        final JobStatusEnum finalJobStatus = immutablePair.getRight();
        final String processInstanceId = immutablePair.getLeft();

        logger.info("Delete Job status: {}", finalJobStatus);

        if (!FINISHED.equals(finalJobStatus)) {

            final Optional<ErrorDetails> optional = workflowQueryService.getErrorDetails(processInstanceId);
            if (optional.isPresent()) {
                final ErrorDetails errorDetails = optional.get();
                final String message = "Failed to Delete AS with id: " + asInstanceId + " due to \n" + errorDetails;
                logger.error(message);
                throw new AsRequestProcessingException(message, errorDetails);
            }

            final String message = "Received unexpected Job Status: " + finalJobStatus
                    + " Failed to Delete AS with id: " + asInstanceId;
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }

        logger.debug("Delete AS finished successfully ...");
    }

    private AsInst getAsInst(final String asInstId) {
        logger.info("Getting AsInst with nsInstId: {}", asInstId);
        final Optional<AsInst> optionalNfvoNsInst = databaseServiceProvider.getAsInst(asInstId);

        if (optionalNfvoNsInst.isEmpty()) {
            final String message = "No matching AS Instance for id: " + asInstId + " found in database.";
            throw new AsRequestProcessingException(message);
        }

        return optionalNfvoNsInst.get();
    }

    private ImmutablePair<String, JobStatusEnum> waitForJobToFinish(final String jobId,
            final ImmutableSet<JobStatusEnum> jobFinishedStates) {
        try {
            final long startTimeInMillis = System.currentTimeMillis();
            final long timeOutTime = startTimeInMillis + TimeUnit.SECONDS.toMillis(timeOutInSeconds);

            logger.info("Will wait till {} for {} job to finish", Instant.ofEpochMilli(timeOutTime).toString(), jobId);
            JobStatusEnum currentJobStatus = null;
            while (timeOutTime > System.currentTimeMillis()) {

                final Optional<Job> optional = databaseServiceProvider.getRefreshedJob(jobId);

                if (optional.isEmpty()) {
                    logger.error("Unable to find Job using jobId: {}", jobId);
                    return ImmutablePair.nullPair();
                }

                final Job job = optional.get();
                currentJobStatus = job.getStatus();
                logger.debug("Received job status response: \n {}", job);
                if (jobFinishedStates.contains(currentJobStatus)) {
                    logger.info("Job finished \n {}", currentJobStatus);
                    return ImmutablePair.of(job.getProcessInstanceId(), currentJobStatus);
                }

                logger.info("Haven't received one of finish state {} yet, will try again in {} seconds",
                        jobFinishedStates, SLEEP_TIME_IN_SECONDS);
                TimeUnit.SECONDS.sleep(SLEEP_TIME_IN_SECONDS);

            }
            logger.warn("Timeout current job status: {}", currentJobStatus);
            return ImmutablePair.nullPair();
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            logger.error("Sleep was interrupted", interruptedException);
            return ImmutablePair.nullPair();
        }
    }

    private void doInitialTerminateChecks(final String asInstanceId, final TerminateAsRequest terminateAsRequest) {
        final AsInst asInst = getAsInst(asInstanceId);
        if (isNotInstantiated(asInst)) {
            final String message = "TerminateAsRequest received: " + terminateAsRequest + " for asInstanceId: "
                    + asInstanceId + "\nUnable to terminate.  AS Instance is already in " + State.NOT_INSTANTIATED
                    + " state." + "\nThis method can only be used with an AS instance in the " + State.INSTANTIATED
                    + " state.";
            logger.error(message);
            throw new AsRequestProcessingException(message);
        }
    }

    private boolean isNotInstantiated(final AsInst asInst) {
        return State.NOT_INSTANTIATED.equals(asInst.getStatus());
    }

    private Map<String, Object> getVariables(final String jobId, final CreateAsRequest createAsRequest) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(JOB_ID_PARAM_NAME, jobId);
        variables.put(CREATE_AS_REQUEST_PARAM_NAME, createAsRequest);
        return variables;
    }

    private Map<String, Object> getVariables(final String asInstanceId, final String jobId, final String occId,
            final InstantiateAsRequest instantiateAsRequest) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(AS_INSTANCE_ID_PARAM_NAME, asInstanceId);
        variables.put(JOB_ID_PARAM_NAME, jobId);
        variables.put(OCC_ID_PARAM_NAME, occId);
        variables.put(INSTANTIATE_AS_REQUEST_PARAM_NAME, instantiateAsRequest);
        return variables;
    }

    private Map<String, Object> getVariables(final String asInstanceId, final String jobId, final String occId,
            final TerminateAsRequest terminateAsRequest) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(AS_INSTANCE_ID_PARAM_NAME, asInstanceId);
        variables.put(JOB_ID_PARAM_NAME, jobId);
        variables.put(OCC_ID_PARAM_NAME, occId);
        variables.put(TERMINATE_AS_REQUEST_PARAM_NAME, terminateAsRequest);
        return variables;
    }

    private Map<String, Object> getVariables(final String asInstanceId, final String jobId) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(AS_INSTANCE_ID_PARAM_NAME, asInstanceId);
        variables.put(JOB_ID_PARAM_NAME, jobId);
        return variables;
    }
}
