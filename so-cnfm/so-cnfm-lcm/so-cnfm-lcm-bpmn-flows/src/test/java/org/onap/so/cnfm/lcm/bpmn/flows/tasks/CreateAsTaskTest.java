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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.aaiclient.client.aai.AAIVersion.V19;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_OWNER_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_REGION_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.SERVICE_INSTANCE_ID_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.SERVICE_INSTANCE_NAME_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.TENANT_ID_PARAM_KEY;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.entities.Results;
import org.onap.aaiclient.client.graphinventory.entities.Resource;
import org.onap.so.cnfm.lcm.bpmn.flows.BaseTest;
import org.onap.so.cnfm.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.AsInstance.InstantiationStateEnum;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class CreateAsTaskTest extends BaseTest {
    private static final String SRC_TEST_DIR = "src/test/resources";
    private static final String TENAT_ID = UUID.randomUUID().toString();
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String CLOUD_REGION = "CloudRegion";
    private static final String ASD_ID = UUID.randomUUID().toString();
    private static final String AS_NAME = "CreateAsService-" + ASD_ID;
    private static final String SDC_GET_RESOURCE_URL = "/sdc/v1/catalog/resources/" + ASD_ID + "/toscaModel";
    private static final String RESOURCE_ASD_PACKAGE_CSAR_PATH =
            SRC_TEST_DIR + "/resource-Generatedasdpackage-csar.csar";

    @Autowired
    private JobExecutorService objUnderTest;

    @Before
    public void before() {
        wireMockServer.resetAll();
    }

    @After
    public void after() {
        wireMockServer.resetAll();
    }

    @Test
    public void testCreateAsWorkflow_SuccessfullCase() throws InterruptedException, IOException {

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(getAbsolutePath(RESOURCE_ASD_PACKAGE_CSAR_PATH)))
                        .withHeader(ACCEPT, APPLICATION_OCTET_STREAM_VALUE)));

        final CreateAsRequest createAsRequest = getCreateAsRequest();

        mockAAIEndpoints();

        final AsInstance nsResponse = objUnderTest.runCreateAsJob(createAsRequest);
        assertNotNull(nsResponse);
        assertNotNull(nsResponse.getAsInstanceid());

        final Optional<Job> optional = getJobByResourceId(createAsRequest.getAsdId());
        assertTrue(optional.isPresent());
        final Job job = optional.get();

        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());
        assertTrue(databaseServiceProvider.isAsInstExists(createAsRequest.getAsInstanceName()));

        final Job actualJob = optional.get();
        assertEquals(JobStatusEnum.FINISHED, actualJob.getStatus());

        assertEquals(AS_NAME, nsResponse.getAsInstanceName());
        assertEquals(InstantiationStateEnum.NOT_INSTANTIATED, nsResponse.getInstantiationState());

        final HistoricVariableInstance doesNsPackageExistsVar =
                getVariable(job.getProcessInstanceId(), "doesAsPackageExists");
        assertNotNull(doesNsPackageExistsVar);
        assertTrue((boolean) doesNsPackageExistsVar.getValue());

        final HistoricVariableInstance doesNsInstanceExistsVar =
                getVariable(job.getProcessInstanceId(), "doesAsInstanceExists");
        assertNotNull(doesNsInstanceExistsVar);
        assertFalse((boolean) doesNsInstanceExistsVar.getValue());

    }

    private CreateAsRequest getCreateAsRequest() {
        return getCreateAsRequest(ASD_ID, AS_NAME);
    }

    private CreateAsRequest getCreateAsRequest(final String asdId, final String asName) {
        final Map<String, Object> additionalParams = Map.of(SERVICE_INSTANCE_ID_PARAM_KEY, SERVICE_INSTANCE_ID,
                SERVICE_INSTANCE_NAME_PARAM_KEY, SERVICE_INSTANCE_NAME, CLOUD_OWNER_PARAM_KEY, CLOUD_OWNER,
                CLOUD_REGION_PARAM_KEY, CLOUD_REGION, TENANT_ID_PARAM_KEY, TENAT_ID);

        return new CreateAsRequest().asdId(asdId).asInstanceName(asName).additionalParams(additionalParams);
    }

    private void mockAAIEndpoints() throws JsonProcessingException {
        final String modelEndpoint = "/aai/" + V19 + "/network/generic-vnfs/generic-vnf/" + UUID_REGEX;

        wireMockServer.stubFor(
                get(urlMatching(modelEndpoint + "\\?resultIndex=0&resultSize=1&format=count")).willReturn(notFound()));

        wireMockServer.stubFor(put(urlMatching(modelEndpoint)).willReturn(ok()));
        wireMockServer.stubFor(put(urlMatching(modelEndpoint + "/relationship-list/relationship")).willReturn(ok()));

        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(ok())
                .willReturn(okJson("{\"orchestration-status\": \"Created\"}")));

        wireMockServer.stubFor(get(urlMatching("/aai/" + V19 + "/nodes/service-instances/service-instance/.*"))
                .willReturn(okJson(getResourceResultsResponseAsJson(SERVICE_INSTANCE_ID))));

        wireMockServer.stubFor(put(urlMatching("/aai/" + V19 + "/cloud-infrastructure/cloud-regions/cloud-region/"
                + CLOUD_OWNER + "/" + CLOUD_REGION + "/tenants/tenant/" + TENAT_ID + "/relationship-list/relationship"))
                        .willReturn(ok()));

    }

    private String getResourceResultsResponseAsJson(final String nsdId) throws JsonProcessingException {
        final Resource resource = new Resource();
        resource.setResourceType("service-instance");
        resource.setResourceLink("/aai/" + V19 + "/business/customers/customer/GLOBAL_CUSTOMER_ID"
                + "/service-subscriptions/service-subscription/NetworkService/service-instances/service-instance/"
                + nsdId);
        final Results<Resource> results = new Results<>();
        results.getResult().add(resource);
        return new ObjectMapper().writeValueAsString(results);
    }

    private Path getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.toPath();
    }

    private byte[] getFileContent(final Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}
