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
package org.onap.so.cnfm.lcm.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.cnfm.lcm.Constants;
import org.onap.so.cnfm.lcm.TestApplication;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.AsRequestProcessingException;
import org.onap.so.cnfm.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.cnfm.lcm.model.AsInfoModificationRequestDeploymentItems;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest.TerminationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.google.gson.Gson;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AsLifecycleManagementControllerTest {

    private static final String BASE_URL = "http://so-cnfm-lcm.onap:9888/so/so-cnfm/v1/api/aslcm/v1";

    private static final String AS_LCM_OPOCC_ID = UUID.randomUUID().toString();
    private static final String AS_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String ASD_ID = UUID.randomUUID().toString();
    private static final String DEPLOYMENT_ITEM_ID = UUID.randomUUID().toString();

    private static final String EXPECTED_AS_LCM_OPOCC_REQUEST_LOCATION_URL =
            BASE_URL + "/as_lcm_op_occs/" + AS_LCM_OPOCC_ID;

    private static final String EXPECTED_CREATE_REQUEST_LOCATION_URL = BASE_URL + "/as_instances/" + AS_INSTANCE_ID;

    @LocalServerPort
    private int port;

    private TestRestTemplate testRestTemplate;

    @Autowired
    private GsonProvider gsonProvider;

    @MockBean
    private JobExecutorService mockedJobExecutorService;

    @Before
    public void setUp() {
        final Gson gson = gsonProvider.getGson();
        testRestTemplate = new TestRestTemplate(
                new RestTemplateBuilder().additionalMessageConverters(new GsonHttpMessageConverter(gson)));
    }

    @Test
    public void testCreateAs_ValidCreateAsRequest_Success() {

        final CreateAsRequest createAsRequest = getCreateAsRequest();

        when(mockedJobExecutorService.runCreateAsJob(createAsRequest))
                .thenReturn(new AsInstance().asInstanceid(AS_INSTANCE_ID));

        final String baseUrl = getAsLcmBaseUrl() + "/as_instances";
        final ResponseEntity<AsInstance> responseEntity = testRestTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(createAsRequest), AsInstance.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());

        final HttpHeaders httpHeaders = responseEntity.getHeaders();
        assertTrue(httpHeaders.containsKey(HttpHeaders.LOCATION));
        final List<String> actual = httpHeaders.get(HttpHeaders.LOCATION);
        assertEquals(1, actual.size());
        assertEquals(EXPECTED_CREATE_REQUEST_LOCATION_URL, actual.get(0));
    }

    @Test
    public void testCreateAs_createAsRequest_asRequestProcessingExceptionThrown_returnErrorDetails() {

        final CreateAsRequest createAsRequest = getCreateAsRequest();

        final String message = "Unable to process request";
        when(mockedJobExecutorService.runCreateAsJob(createAsRequest))
                .thenThrow(new AsRequestProcessingException(message, new ErrorDetails().detail(message)));

        final String baseUrl = getAsLcmBaseUrl() + "/as_instances";
        final ResponseEntity<ErrorDetails> responseEntity = testRestTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(createAsRequest), ErrorDetails.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());

        final ErrorDetails body = responseEntity.getBody();
        assertEquals(message, body.getDetail());

    }

    @Test
    public void testCreateAs_createAsRequest_runTimeExceptionThrown_returnErrorDetails() {

        final CreateAsRequest createAsRequest = getCreateAsRequest();

        final String message = "Unable to process request";
        when(mockedJobExecutorService.runCreateAsJob(createAsRequest)).thenThrow(new RuntimeException(message));

        final String baseUrl = getAsLcmBaseUrl() + "/as_instances";
        final ResponseEntity<ErrorDetails> responseEntity = testRestTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(createAsRequest), ErrorDetails.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());

        final ErrorDetails body = responseEntity.getBody();
        assertEquals(message, body.getDetail());
    }

    @Test
    public void testinstantiateAs_validInstantiateAsRequest_Success() {

        final InstantiateAsRequest instantiateAsRequest = getInstantiateAsRequest();

        when(mockedJobExecutorService.runInstantiateAsJob(AS_INSTANCE_ID, instantiateAsRequest))
                .thenReturn(AS_LCM_OPOCC_ID);

        final String baseUrl = getAsLcmBaseUrl() + "/as_instances/" + AS_INSTANCE_ID + "/instantiate";
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(instantiateAsRequest), Void.class);
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        final HttpHeaders httpHeaders = responseEntity.getHeaders();
        assertTrue(httpHeaders.containsKey(HttpHeaders.LOCATION));
        final List<String> actual = httpHeaders.get(HttpHeaders.LOCATION);
        assertEquals(1, actual.size());
        assertEquals(EXPECTED_AS_LCM_OPOCC_REQUEST_LOCATION_URL, actual.get(0));
    }

    @Test
    public void testinstantiateAs_validInstantiateAsRequest_asRequestProcessingExceptionThrown_returnErrorDetails() {

        final InstantiateAsRequest instantiateAsRequest = getInstantiateAsRequest();

        when(mockedJobExecutorService.runInstantiateAsJob(AS_INSTANCE_ID, instantiateAsRequest))
                .thenThrow(new AsRequestProcessingException("failed"));

        final String baseUrl = getAsLcmBaseUrl() + "/as_instances/" + AS_INSTANCE_ID + "/instantiate";
        final ResponseEntity<ErrorDetails> responseEntity = testRestTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(instantiateAsRequest), ErrorDetails.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testTerminateAs_ValidTerminateAsRequest_Success() {

        final TerminateAsRequest terminateAsRequest =
                new TerminateAsRequest().terminationType(TerminationTypeEnum.GRACEFUL);

        when(mockedJobExecutorService.runTerminateAsJob(AS_INSTANCE_ID, terminateAsRequest))
                .thenReturn(AS_LCM_OPOCC_ID);

        final String baseUrl = getAsLcmBaseUrl() + "/as_instances/" + AS_INSTANCE_ID + "/terminate";
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(terminateAsRequest), Void.class);
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        final HttpHeaders httpHeaders = responseEntity.getHeaders();
        assertTrue(httpHeaders.containsKey(HttpHeaders.LOCATION));
        final List<String> actual = httpHeaders.get(HttpHeaders.LOCATION);
        assertEquals(1, actual.size());
        assertEquals(EXPECTED_AS_LCM_OPOCC_REQUEST_LOCATION_URL, actual.get(0));

    }

    @Test
    public void testDeleteAs_ValidRequest_Success() {

        doNothing().when(mockedJobExecutorService).runDeleteAsJob(AS_INSTANCE_ID);
        final String baseUrl = getAsLcmBaseUrl() + "/as_instances/" + AS_INSTANCE_ID;
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    private InstantiateAsRequest getInstantiateAsRequest() {
        return new InstantiateAsRequest().addDeploymentItemsItem(
                new AsInfoModificationRequestDeploymentItems().deploymentItemsId(DEPLOYMENT_ITEM_ID));
    }

    private CreateAsRequest getCreateAsRequest() {
        return new CreateAsRequest().asdId(ASD_ID);
    }

    private String getAsLcmBaseUrl() {
        return "http://localhost:" + port + Constants.AS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
    }
}
