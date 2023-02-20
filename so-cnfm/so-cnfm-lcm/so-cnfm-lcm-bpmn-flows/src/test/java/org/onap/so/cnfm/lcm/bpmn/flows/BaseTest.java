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

package org.onap.so.cnfm.lcm.bpmn.flows;

import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_ACTIVE;
import static org.slf4j.LoggerFactory.getLogger;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.runner.RunWith;
import org.onap.so.cnfm.lcm.bpmn.flows.service.KubConfigProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.tasks.MockedHelmClientConfiguration;
import org.onap.so.cnfm.lcm.bpmn.flows.tasks.MockedKubernetesClientProviderConfiguration;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobAction;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureWireMock(port = 0)
@Import({MockedHelmClientConfiguration.class, MockedKubernetesClientProviderConfiguration.class})
public abstract class BaseTest {
    protected static final String SERVICE_INSTANCE_ID = UUID.randomUUID().toString();
    protected static final String SERVICE_INSTANCE_NAME = "ServiceName";
    private static final String KUBE_CONFIG_EMPTY_FILE_NAME = "kube-config-empty-file";
    private static final String EMPTY = "";

    protected static final String UUID_REGEX =
            "[0-9a-zA-Z]{8}\\-[0-9a-zA-Z]{4}\\-[0-9a-zA-Z]{4}\\-[0-9a-zA-Z]{4}\\-[0-9a-zA-Z]{12}";
    protected static final String RANDOM_JOB_ID = UUID.randomUUID().toString();
    protected static final Logger logger = getLogger(BaseTest.class);

    private static final long TIME_OUT_IN_SECONDS = 120;
    private static final int SLEEP_TIME_IN_SECONDS = 5;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private KubConfigProvider kubConfigProvider;

    @Autowired
    protected DatabaseServiceProvider databaseServiceProvider;

    @Autowired
    protected WireMockServer wireMockServer;

    public Job createNewJob(final String jobAction, final String nsdId, final String nsName) {
        final Job newJob = new Job().startTime(LocalDateTime.now()).jobType("AS").jobAction(JobAction.CREATE)
                .status(JobStatusEnum.STARTING).resourceId(nsdId).resourceName(nsName);
        databaseServiceProvider.addJob(newJob);
        return newJob;
    }

    public Optional<Job> getJob(final String jobId) {
        return databaseServiceProvider.getJob(jobId);
    }

    public Optional<Job> getJobByResourceId(final String resourceId) {
        return databaseServiceProvider.getJobByResourceId(resourceId);
    }

    public ProcessInstance executeWorkflow(final String processDefinitionKey, final String businessKey,
            final Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    public HistoricProcessInstance getHistoricProcessInstance(final String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    public HistoricVariableInstance getVariable(final String processInstanceId, final String name) {
        return historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                .variableName(name).singleResult();
    }

    public boolean waitForProcessInstanceToFinish(final String processInstanceId) throws InterruptedException {
        final long startTimeInMillis = System.currentTimeMillis();
        final long timeOutTime = startTimeInMillis + TimeUnit.SECONDS.toMillis(TIME_OUT_IN_SECONDS);
        while (timeOutTime > System.currentTimeMillis()) {

            if (isProcessEndedByProcessInstanceId(processInstanceId)) {
                logger.info("processInstanceId: {} is finished", processInstanceId);
                return true;
            }
            logger.info("processInstanceId: {} is still running", processInstanceId);
            logger.info("Process instance {} not finished yet, will try again in {} seconds", processInstanceId,
                    SLEEP_TIME_IN_SECONDS);
            TimeUnit.SECONDS.sleep(SLEEP_TIME_IN_SECONDS);
        }
        logger.warn("Timeout {} process didn't finished ", processInstanceId);
        return false;
    }


    public boolean isProcessEndedByProcessInstanceId(final String processInstanceId) {
        return !isProcessInstanceActive(processInstanceId) && isProcessInstanceEnded(processInstanceId)
                && isProcessInstanceCompleted(processInstanceId);
    }

    private boolean isProcessInstanceActive(final String processInstanceId) {
        final HistoricProcessInstance processInstance = getHistoricProcessInstance(processInstanceId);
        return processInstance != null && STATE_ACTIVE.equalsIgnoreCase(processInstance.getState());
    }

    private boolean isProcessInstanceEnded(final String processInstanceId) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult() == null;
    }

    private boolean isProcessInstanceCompleted(final String processInstanceId) {
        final HistoricProcessInstance result =
                historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return result == null ? false : HistoricProcessInstance.STATE_COMPLETED.equalsIgnoreCase(result.getState());
    }

    public void createKubeConfigFile(final AsInst asInst) throws IOException {
        final MockMultipartFile file = new MockMultipartFile(KUBE_CONFIG_EMPTY_FILE_NAME, EMPTY.getBytes());
        kubConfigProvider.addKubeConfigFile(file, asInst.getCloudOwner(), asInst.getCloudRegion(),
                asInst.getTenantId());
    }

    public void deleteFoldersAndFiles(final Path path) throws IOException {
        FileSystemUtils.deleteRecursively(path);
    }

}
