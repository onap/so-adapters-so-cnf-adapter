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

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.aaiclient.client.aai.AAIVersion.V19;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.cnfm.lcm.bpmn.flows.BaseTest;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.AsRequestProcessingException;
import org.onap.so.cnfm.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.cnfm.lcm.bpmn.flows.service.WorkflowQueryService;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class DeleteAsTaskTest extends BaseTest {

    @Autowired
    private JobExecutorService objUnderTest;

    @Autowired
    private WorkflowQueryService workflowQueryService;

    @Before
    public void before() {
        wireMockServer.resetAll();
    }

    @After
    public void after() {
        wireMockServer.resetAll();
    }

    @Test
    public void testRunDeleteNsJob_SuccessfulCase() throws InterruptedException {
        final String asInstanceId = UUID.randomUUID().toString();
        addDummyAsToDatabase(asInstanceId, State.NOT_INSTANTIATED);
        mockAaiEndpoints(asInstanceId);

        objUnderTest.runDeleteAsJob(asInstanceId);

        final Optional<Job> optional = getJobByResourceId(asInstanceId);
        assertTrue(optional.isPresent());
        final Job job = optional.get();

        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final Optional<AsInst> optionalAsInst = databaseServiceProvider.getAsInst(asInstanceId);
        assertTrue(optionalAsInst.isEmpty());

    }

    @Test
    public void testRunDeleteNsJob_AsInstanceDoesNotExistsInDb() throws InterruptedException {
        final String asInstanceId = UUID.randomUUID().toString();

        try {
            objUnderTest.runDeleteAsJob(asInstanceId);
        } catch (final Exception exception) {
            assertEquals(AsRequestProcessingException.class, exception.getClass());
        }

        final Optional<Job> optional = getJobByResourceId(asInstanceId);
        assertTrue(optional.isPresent());
        final Job job = optional.get();

        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final Optional<AsInst> optionalAsInst = databaseServiceProvider.getAsInst(asInstanceId);
        assertTrue(optionalAsInst.isEmpty());

        final Optional<ErrorDetails> errorDetailsOptional =
                workflowQueryService.getErrorDetails(job.getProcessInstanceId());
        assertTrue(errorDetailsOptional.isPresent());

        final ErrorDetails errorDetails = errorDetailsOptional.get();
        assertNotNull(errorDetails);
        assertNotNull(errorDetails.getDetail());

        final HistoricVariableInstance doesAsInstanceExistsVar =
                getVariable(job.getProcessInstanceId(), "asInstanceExists");
        assertNotNull(doesAsInstanceExistsVar);
        assertFalse((boolean) doesAsInstanceExistsVar.getValue());
    }


    @Test
    public void testRunDeleteNsJob_AsInstanceDoesExistsInDbWithInstantiatedState() throws InterruptedException {
        final String asInstanceId = UUID.randomUUID().toString();

        addDummyAsToDatabase(asInstanceId, State.INSTANTIATED);

        try {
            objUnderTest.runDeleteAsJob(asInstanceId);
        } catch (final Exception exception) {
            assertEquals(AsRequestProcessingException.class, exception.getClass());
        }

        final Optional<Job> optional = getJobByResourceId(asInstanceId);
        assertTrue(optional.isPresent());
        final Job job = optional.get();

        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final Optional<AsInst> optionalAsInst = databaseServiceProvider.getAsInst(asInstanceId);
        assertFalse(optionalAsInst.isEmpty());

        final Optional<ErrorDetails> errorDetailsOptional =
                workflowQueryService.getErrorDetails(job.getProcessInstanceId());
        assertTrue(errorDetailsOptional.isPresent());

        final ErrorDetails errorDetails = errorDetailsOptional.get();
        assertNotNull(errorDetails);
        assertNotNull(errorDetails.getDetail());

        final HistoricVariableInstance isInNotInstantiatedStateVar =
                getVariable(job.getProcessInstanceId(), "isInNotInstantiatedState");
        assertNotNull(isInNotInstantiatedStateVar);
        assertFalse((boolean) isInNotInstantiatedStateVar.getValue());
    }

    private void mockAaiEndpoints(final String asInstanceId) {
        final String modelEndpoint = "/aai/" + V19 + "/network/generic-vnfs/generic-vnf/" + asInstanceId;
        final String resourceVersion = UUID.randomUUID().toString();

        final String body =
                "{\"resource-version\": \"" + resourceVersion + "\",\n\"orchestration-status\": \"Assigned\"}";
        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(ok()).willReturn(okJson(body)));
        wireMockServer.stubFor(
                delete(urlMatching(modelEndpoint + "\\?resource-version=" + resourceVersion)).willReturn(ok()));
    }

    private void addDummyAsToDatabase(final String asInstanceId, final State state) {
        final String asdId = UUID.randomUUID().toString();

        final AsInst asInst = new AsInst().asInstId(asInstanceId).name("asName").asdId(asdId)
                .asdInvariantId(asInstanceId).status(state).statusUpdatedTime(LocalDateTime.now())
                .asApplicationName("asApplicationName").asApplicationVersion("asApplicationVersion")
                .asProvider("asProvider").serviceInstanceId(SERVICE_INSTANCE_ID)
                .serviceInstanceName("serviceInstanceName").cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId").namespace("namespace");
        databaseServiceProvider.saveAsInst(asInst);
    }

}
