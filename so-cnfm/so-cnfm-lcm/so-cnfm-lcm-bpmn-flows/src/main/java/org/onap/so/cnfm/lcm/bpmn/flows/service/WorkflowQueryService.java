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

import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_AS_RESPONSE_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.TENANT_ID;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.base.Strings;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class WorkflowQueryService {

    private static final Logger logger = getLogger(WorkflowQueryService.class);

    private final HistoryService camundaHistoryService;

    @Autowired
    public WorkflowQueryService(final HistoryService camundaHistoryService) {
        this.camundaHistoryService = camundaHistoryService;
    }

    public Optional<AsInstance> getCreateNsResponse(final String processInstanceId) {
        try {

            if (Strings.isNullOrEmpty(processInstanceId)) {
                logger.error("Invalid processInstanceId: {}", processInstanceId);
                return Optional.empty();
            }

            final HistoricVariableInstance historicVariableInstance =
                    getVariable(processInstanceId, CREATE_AS_RESPONSE_PARAM_NAME);

            if (historicVariableInstance != null) {
                logger.info("Found HistoricVariableInstance : {}", historicVariableInstance);
                final Object variableValue = historicVariableInstance.getValue();
                if (variableValue instanceof AsInstance) {
                    return Optional.ofNullable((AsInstance) variableValue);
                }
                logger.error("Unknown CreateAsResponse object type {} received value: {}",
                        historicVariableInstance.getValue() != null ? variableValue.getClass() : null, variableValue);
            }
        } catch (final ProcessEngineException processEngineException) {
            logger.error("Unable to find {} variable using processInstanceId: {}", CREATE_AS_RESPONSE_PARAM_NAME,
                    processInstanceId, processEngineException);
        }
        logger.error("Unable to find {} variable using processInstanceId: {}", CREATE_AS_RESPONSE_PARAM_NAME,
                processInstanceId);
        return Optional.empty();

    }

    public Optional<ErrorDetails> getErrorDetails(final String processInstanceId) {
        try {
            final HistoricVariableInstance historicVariableInstance =
                    getVariable(processInstanceId, AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME);

            logger.info("Found HistoricVariableInstance : {}", historicVariableInstance);
            if (historicVariableInstance != null) {
                final Object variableValue = historicVariableInstance.getValue();
                if (variableValue instanceof ErrorDetails) {
                    return Optional.ofNullable((ErrorDetails) variableValue);
                }
                logger.error("Unknown ErrorContents object type {} received value: {}",
                        historicVariableInstance.getValue() != null ? variableValue.getClass() : null, variableValue);
            }
            logger.error("Unable to retrieve HistoricVariableInstance value was null");
        } catch (final ProcessEngineException processEngineException) {
            logger.error("Unable to find {} variable using processInstanceId: {}",
                    AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, processInstanceId, processEngineException);
        }
        return Optional.empty();
    }


    private HistoricVariableInstance getVariable(final String processInstanceId, final String name) {
        return camundaHistoryService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                .variableName(name).tenantIdIn(TENANT_ID).singleResult();
    }

}
