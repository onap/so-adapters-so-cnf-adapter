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
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.cnfm.lcm.Constants;
import org.onap.so.cnfm.lcm.TestApplication;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpType;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class AsLcmOperationOccurrencesControllerTest {

    private static final String RANDOM_UUID = UUID.randomUUID().toString();

    private static final String AS_LCM_OP_OCCS = "/as_lcm_op_occs/";

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Autowired
    private GsonProvider gsonProvider;

    private TestRestTemplate testRestTemplate;

    @Before
    public void setUp() {
        final Gson gson = gsonProvider.getGson();
        testRestTemplate = new TestRestTemplate(
                new RestTemplateBuilder().additionalMessageConverters(new GsonHttpMessageConverter(gson)));
    }

    @Test
    public void testGetOperationStatus_validAsLcmOpOccId_returnsAsLcmOpOcc() {
        final String asLcmOpOccId = addDummyAsLcmOpOccToDatabase();
        final String baseUrl = getAsLcmBaseUrl() + AS_LCM_OP_OCCS + asLcmOpOccId;
        final HttpEntity<?> request = new HttpEntity<>(new HttpHeaders());
        final ResponseEntity<AsLcmOpOcc> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.GET, request, AsLcmOpOcc.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testGetOperationStatus_invalidAsLcmOpOccId_returnsErrorDetails() {
        final String baseUrl = getAsLcmBaseUrl() + AS_LCM_OP_OCCS + "123";
        final HttpEntity<?> request = new HttpEntity<>(new HttpHeaders());
        final ResponseEntity<ErrorDetails> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.GET, request, ErrorDetails.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());
    }

    private String getAsLcmBaseUrl() {
        return "http://localhost:" + port + Constants.AS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
    }

    private String addDummyAsLcmOpOccToDatabase() {
        final LocalDateTime now = LocalDateTime.now();
        final AsInst asInst = new AsInst().name("name").asdId(RANDOM_UUID).status(State.NOT_INSTANTIATED)
                .asdInvariantId(RANDOM_UUID).statusUpdatedTime(now).asApplicationName("asApplicationName")
                .asApplicationVersion("asApplicationVersion").asProvider("asProvider").serviceInstanceId(RANDOM_UUID)
                .serviceInstanceName("serviceInstanceName").cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId");

        databaseServiceProvider.saveAsInst(asInst);

        final org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc databaseEntry =
                new org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc();

        databaseEntry.asInst(asInst).operationState(OperationStateEnum.PROCESSING).isCancelPending(false)
                .isAutoInvocation(false).operation(AsLcmOpType.INSTANTIATE).startTime(now).stateEnteredTime(now)
                .operationParams("");

        databaseServiceProvider.addAsLcmOpOcc(databaseEntry);

        return databaseEntry.getId();
    }

}
