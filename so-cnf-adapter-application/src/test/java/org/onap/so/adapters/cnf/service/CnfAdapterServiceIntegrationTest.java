/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.cnf.service;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityNotFoundException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CnfAdapterServiceIntegrationTest {

    private static int wireMockPort = SocketUtils.findAvailableTcpPort();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(wireMockPort));

    @Autowired
    private CnfAdapterService cnfAdapterService;

    @Before
    public void setUp() {
        WireMock.reset();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("multicloud.endpoint", () -> "http://localhost:" + wireMockPort);
        registry.add("spring.security.usercredentials[0].username", () -> "test");
        registry.add("spring.security.usercredentials[0].password", () -> "test");
        registry.add("spring.security.usercredentials[0].role", () -> "ACTUATOR");
    }

    @Test
    public void thatInstanceCanBeCreated() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/createInstanceResponse.json")));

        BpmnInstanceRequest request = new BpmnInstanceRequest();
        request.setModelInvariantId("rb-name-1");
        request.setModelCustomizationId("rb-version-1");
        request.setK8sRBProfileName("profile-1");
        request.setCloudRegionId("region-1");
        request.setK8sRBInstanceReleaseName("test-release");

        String result = cnfAdapterService.createInstance(request);

        assertNotNull(result);
        assertTrue(result.contains("new-inst"));
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/instance"))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-name']",
                        WireMock.equalTo("rb-name-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-version']",
                        WireMock.equalTo("rb-version-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['profile-name']",
                        WireMock.equalTo("profile-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['cloud-region']",
                        WireMock.equalTo("region-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['release-name']",
                        WireMock.equalTo("test-release"))));
    }

    @Test
    public void thatInstanceCanBeRetrievedById() throws Exception {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance/inst-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/getInstanceResponse.json")));

        String result = cnfAdapterService.getInstanceByInstanceId("inst-1");

        assertNotNull(result);
        assertTrue(result.contains("inst-1"));
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/instance/inst-1"))
                .withHeader("Accept", WireMock.equalTo("application/json")));
    }

    @Test
    public void thatInstanceCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/v1/instance/inst-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        cnfAdapterService.deleteInstanceByInstanceId("inst-1");

        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/instance/inst-1")));
    }

    @Test
    public void thatInstanceStatusCanBeRetrieved() throws Exception {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance/inst-1/status"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/getInstanceStatusResponse.json")));

        String result = cnfAdapterService.getInstanceStatusByInstanceId("inst-1");

        assertNotNull(result);
        assertTrue(result.contains("resourceCount"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/instance/inst-1/status")));
    }

    @Test
    public void thatInstanceResourcesCanBeQueried() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/instance/inst-1/query"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/queryInstanceResourcesResponse.json")));

        String result = cnfAdapterService.queryInstanceResources(
                "inst-1", "Pod", "v1", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("test-pod"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlPathEqualTo("/v1/instance/inst-1/query"))
                .withQueryParam("Kind", WireMock.equalTo("Pod"))
                .withQueryParam("ApiVersion", WireMock.equalTo("v1")));
    }

    @Test(expected = EntityNotFoundException.class)
    public void thatCreateInstanceThrowsOnNotFound() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withBodyFile("multicloud/notFoundResponse.json")));

        BpmnInstanceRequest request = new BpmnInstanceRequest();
        request.setModelInvariantId("rb-name-1");
        request.setModelCustomizationId("rb-version-1");
        request.setK8sRBProfileName("profile-1");
        request.setCloudRegionId("region-1");
        request.setK8sRBInstanceReleaseName("test-release");

        cnfAdapterService.createInstance(request);
    }
}
