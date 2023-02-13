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
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.cnfm.lcm.database.beans.JobStatusEnum.STARTED;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class DeleteAsTask extends AbstractServiceTask {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAsTask.class);

    private static final String AS_INSTANCE_EXISTS_PARAM_NAME = "asInstanceExists";
    private static final String AS_INSTANCE_IS_IN_NOT_INSTANTIATED_STATE_PARAM_NAME = "isInNotInstantiatedState";

    private final AaiServiceProvider aaiServiceProvider;

    protected DeleteAsTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, STARTED, "Delete AS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, FINISHED, "Delete AS workflow process finished");
    }

    public void setJobStatusInProgress(final DelegateExecution execution, final String message) {
        setJobStatus(execution, IN_PROGRESS, message);
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        setJobStatusToError(execution, "Delete AS workflow process failed");
    }

    public void checkIfAsInstanceExistsInDb(final DelegateExecution execution) {
        logger.info("Executing checkIfAsInstanceExistsInDb  ...");
        setJobStatusInProgress(execution, "Checking that AS Instance Exists in DB");

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final Optional<AsInst> optionalAsInst = databaseServiceProvider.getAsInst(asInstId);
        final boolean asInstanceExists = optionalAsInst.isPresent();
        logger.info("AS Instance entry with id: {} {} exist in database", asInstId,
                asInstanceExists ? "does" : "doesn't");
        execution.setVariable(AS_INSTANCE_EXISTS_PARAM_NAME, asInstanceExists);

        if (!asInstanceExists) {
            final String message =
                    "AS Instance with id: " + asInstId + " does not exist in database, so will not be deleted.";
            logger.info(message);
            execution.setVariable(AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, new ErrorDetails().detail(message));
        }

        logger.info("Finished executing checkIfAsInstanceExistsInDb ...");
    }

    public void checkifAsInstanceInDbIsInNotInstantiatedState(final DelegateExecution execution) {
        logger.info("Executing checkifAsInstanceInDbIsInNotInstantiatedState ...");
        setJobStatusInProgress(execution, "Checking if AS Instance in database is in NOT_INSTANTIATED state");

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        final AsInst asInst = getAsInst(execution, asInstId);
        final State state = asInst.getStatus();
        final boolean isInNotInstantiatedState = State.NOT_INSTANTIATED.equals(state);
        logger.info("As Instance entry with asInstId: {} is in state: {}", asInstId, state);
        execution.setVariable(AS_INSTANCE_IS_IN_NOT_INSTANTIATED_STATE_PARAM_NAME, isInNotInstantiatedState);

        if (!isInNotInstantiatedState) {
            final String message = "Cannot Delete AS Instance with id: " + asInstId + " in the state: " + state;
            logger.info(message);
            execution.setVariable(AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, new ErrorDetails().detail(message));
        }

        logger.info("Finished executing checkifAsInstanceInDbIsInNotInstantiatedState ...");
    }

    public void deleteGenericVnfFromAai(final DelegateExecution execution) {
        logger.info("Executing deleteGenericVnfFromAai ...");
        try {
            final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
            aaiServiceProvider.deleteGenericVnf(asInstId);
        } catch (final Exception exception) {
            final String message = "Unable to Delete GenericVnf from AAI ";
            logger.error(message, exception);
            abortOperation(execution, message);
        }


        logger.info("Finished executing deleteGenericVnfFromAai ...");
    }

    public void deleteAsInstanceFromDb(final DelegateExecution execution) {
        logger.info("Executing deleteAsInstanceFromDb ...");
        setJobStatusInProgress(execution, "Deleting AS Instance from Db");

        final String asInstId = (String) execution.getVariable(AS_INSTANCE_ID_PARAM_NAME);
        databaseServiceProvider.deleteAsInst(asInstId);

        logger.info("Finished executing deleteAsInstanceFromDb ...");
    }
}
