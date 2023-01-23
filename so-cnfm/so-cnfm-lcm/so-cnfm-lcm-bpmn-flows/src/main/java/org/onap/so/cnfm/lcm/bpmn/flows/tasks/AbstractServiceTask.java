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
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.JOB_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.OCC_ID_PARAM_NAME;

import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.IN_PROGRESS;

import java.time.LocalDateTime;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobStatus;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public abstract class AbstractServiceTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DatabaseServiceProvider databaseServiceProvider;

    protected AbstractServiceTask(final DatabaseServiceProvider databaseServiceProvider) {
        this.databaseServiceProvider = databaseServiceProvider;
    }

    public void addJobStatus(final DelegateExecution execution, final JobStatusEnum jobStatusEnum,
            final String description) {
        final JobStatus jobStatus =
                new JobStatus().status(jobStatusEnum).description(description).updatedTime(LocalDateTime.now());
        logger.info("Adding JobStatus {}", jobStatus);
        final Job job = getJob(execution);
        job.jobStatus(jobStatus);
        databaseServiceProvider.addJob(job);
    }

    public void setJobStatus(final DelegateExecution execution, final JobStatusEnum jobStatus,
            final String description) {
        logger.info("Setting Job Status to {}", jobStatus);
        final Job job = getJob(execution);
        job.status(jobStatus);
        if (JobStatusEnum.STARTED.equals(jobStatus)) {
            job.processInstanceId(execution.getProcessInstanceId());
        }

        if (JobStatusEnum.FINISHED.equals(jobStatus)) {
            job.endTime(LocalDateTime.now());
        }

        job.jobStatus(new JobStatus().status(jobStatus).description(description).updatedTime(LocalDateTime.now()));
        databaseServiceProvider.addJob(job);

    }

    public void setAsInstanceStatusToFailed(final DelegateExecution execution) {
        logger.info("Setting As Instance Status to {}", State.FAILED);
        updateAsInstanceStatus(execution, State.FAILED);
    }

    public void setJobStatusToError(final DelegateExecution execution, final String description) {
        logger.info("Setting Job Status to {}", JobStatusEnum.ERROR);

        final String jobId = (String) execution.getVariable(JOB_ID_PARAM_NAME);
        final Optional<Job> optional = databaseServiceProvider.getJob(jobId);
        if (optional.isPresent()) {
            final ErrorDetails errorDetails =
                    (ErrorDetails) execution.getVariable(AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME);

            final Job job = optional.get();
            job.status(JobStatusEnum.ERROR).endTime(LocalDateTime.now());

            if (errorDetails != null) {
                logger.error("Found failed reason: {}", errorDetails);
                job.jobStatus(new JobStatus().status(JobStatusEnum.ERROR).description(errorDetails.getDetail())
                        .updatedTime(LocalDateTime.now()));
            }
            job.jobStatus(new JobStatus().status(JobStatusEnum.ERROR).description(description)
                    .updatedTime(LocalDateTime.now()));

            databaseServiceProvider.addJob(job);
        }
        logger.info("Finished setting Job Status to {}", JobStatusEnum.ERROR);

    }

    protected void abortOperation(final DelegateExecution execution, final String message) {
        abortOperation(execution, message, new ErrorDetails().detail(message));
    }

    protected void abortOperation(final DelegateExecution execution, final String message,
            final ErrorDetails problemDetails) {
        logger.error(message);
        execution.setVariable(AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, problemDetails);
        throw new BpmnError("WORKFLOW_FAILED", message);
    }

    private Job getJob(final DelegateExecution execution) {
        final String jobId = (String) execution.getVariable(JOB_ID_PARAM_NAME);
        final Optional<Job> optional = databaseServiceProvider.getJob(jobId);
        if (optional.isEmpty()) {
            final String message = "Unable to find job using job id: " + jobId;
            logger.error(message);
            abortOperation(execution, message);
        }

        return optional.get();
    }

    protected void updateAsInstanceStatus(final DelegateExecution execution, final State nsStatus) {
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);

        logger.info("Updating AsInst Status to {} and saving to DB", nsStatus);
        databaseServiceProvider.updateAsInstState(asInstId, nsStatus);
    }

    protected AsInst getAsInst(final DelegateExecution execution) {
        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        return getAsInst(execution, asInstId);
    }

    protected AsInst getAsInst(final DelegateExecution execution, final String asInstId) {
        logger.info("Getting AsInst to update with asInstId: {}", asInstId);
        final Optional<AsInst> optionalAsInst = databaseServiceProvider.getAsInst(asInstId);

        if (optionalAsInst.isEmpty()) {
            final String message = "Unable to find AS Instance in database using id: " + asInstId;
            abortOperation(execution, message);
        }

        return optionalAsInst.get();
    }

    public void updateAsLcmOpOccStatusToCompleted(final DelegateExecution execution) {
        logger.info("Executing updateAsLcmOpOccStatusToCompleted ...");

        addJobStatus(execution, IN_PROGRESS, "Updating AsLcmOpOcc Status to " + OperationStateEnum.COMPLETED);
        updateAsLcmOpOccOperationState(execution, OperationStateEnum.COMPLETED);

        logger.info("Finished executing updateAsLcmOpOccStatusToCompleted ...");

    }

    public void updateAsLcmOpOccStatusToFailed(final DelegateExecution execution) {
        logger.info("Executing updateAsLcmOpOccStatusToFailed ...");

        updateAsLcmOpOccOperationState(execution, OperationStateEnum.FAILED);

        logger.info("Finished executing updateAsLcmOpOccStatusToFailed ...");

    }

    private void updateAsLcmOpOccOperationState(final DelegateExecution execution,
            final OperationStateEnum operationState) {
        final String occId = (String) execution.getVariable(OCC_ID_PARAM_NAME);

        final boolean isSuccessful = databaseServiceProvider.updateAsLcmOpOccOperationState(occId, operationState);
        if (!isSuccessful) {
            final String message =
                    "Unable to update AsLcmOpOcc " + occId + " operationState to" + operationState + " in database";
            logger.error(message);
            abortOperation(execution, message);
        }
    }


    public void updateDeploymentItemStatus(final DelegateExecution execution, final String asDeploymentItemInstId,
            final State state) {
        logger.debug("updateDeploymentItemStatus to status: {}", state);
        final boolean isSuccessful = databaseServiceProvider.updateAsDeploymentItemState(asDeploymentItemInstId, state);
        if (!isSuccessful) {
            final String message = "Unable to update AsDeploymentItem " + asDeploymentItemInstId + " status to" + state
                    + " in database";
            logger.error(message);
            abortOperation(execution, message);
        }
    }

}
