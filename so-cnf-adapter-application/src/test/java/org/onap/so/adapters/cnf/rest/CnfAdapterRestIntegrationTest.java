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

package org.onap.so.adapters.cnf.rest;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.upgrade.InstanceUpgradeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Integration tests for {@link CnfAdapterRest} that start from HTTP requests
 * to the controller endpoints and verify that the correct outbound requests
 * are made to the Multicloud K8s Plugin (stubbed by WireMock).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CnfAdapterRestIntegrationTest {

    private static int wireMockPort = SocketUtils.findAvailableTcpPort();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(wireMockPort));

    @Autowired
    private TestRestTemplate restTemplate;

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

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/instance", request, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("new-inst"));
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/instance"))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-name']",
                        WireMock.equalTo("rb-name-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-version']",
                        WireMock.equalTo("rb-version-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['profile-name']",
                        WireMock.equalTo("profile-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['cloud-region']",
                        WireMock.equalTo("region-1"))));
    }

    @Test
    public void thatInstanceCanBeRetrievedById() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance/inst-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/getInstanceResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/instance/{instID}", String.class, "inst-1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("inst-1"));
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/instance/inst-1")));
    }

    @Test
    public void thatInstanceCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/v1/instance/inst-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/instance/{instID}", HttpMethod.DELETE,
                null, String.class, "inst-1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/instance/inst-1")));
    }

    @Test
    public void thatInstanceStatusCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance/inst-1/status"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/getInstanceStatusResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/instance/{instID}/status", String.class, "inst-1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("resourceCount"));
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

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/instance/{instanceId}/query?Kind={kind}&ApiVersion={apiVersion}",
                String.class, "inst-1", "Pod", "v1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-pod"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlPathEqualTo("/v1/instance/inst-1/query"))
                .withQueryParam("Kind", WireMock.equalTo("Pod"))
                .withQueryParam("ApiVersion", WireMock.equalTo("v1")));
    }

    @Test
    public void thatInstanceCanBeUpgraded() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance/test-inst/upgrade"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/upgradeInstanceResponse.json")));

        InstanceUpgradeRequest upgradeRequest = new InstanceUpgradeRequest();
        upgradeRequest.setModelInvariantId("rb-name-1");
        upgradeRequest.setModelCustomizationId("rb-version-1");
        upgradeRequest.setK8sRBProfileName("default");
        upgradeRequest.setCloudRegionId("region-1");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/instance/{instanceID}/upgrade",
                upgradeRequest, String.class, "test-inst");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/upgrade"))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-name']",
                        WireMock.equalTo("rb-name-1")))
                .withRequestBody(WireMock.matchingJsonPath("$['profile-name']",
                        WireMock.equalTo("default"))));
    }

    @Test
    public void thatStatusCheckCallsMulticloudAndSendsCallback() throws InterruptedException {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance/test-inst/status"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/instanceStatusResponse.json")));

        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/callback"))
                .willReturn(WireMock.aResponse().withStatus(200)));

        CheckInstanceRequest statusCheckRequest = new CheckInstanceRequest();
        InstanceRequest instanceRequest = new InstanceRequest();
        instanceRequest.setInstanceId("test-inst");
        statusCheckRequest.setInstances(Collections.singletonList(instanceRequest));
        statusCheckRequest.setCallbackUrl("http://localhost:" + wireMockPort + "/callback");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/statuscheck", statusCheckRequest, String.class);

        assertEquals(202, response.getStatusCodeValue());

        awaitWireMockCallback("/callback");

        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/status")));
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/callback"))
                .withHeader("Content-Type", WireMock.containing("text/plain"))
                .withBasicAuth(new BasicCredentials("test", "test")));
    }

    @Test
    public void thatCreateInstanceReturns500OnMulticloud404() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withBodyFile("multicloud/notFoundResponse.json")));

        BpmnInstanceRequest request = new BpmnInstanceRequest();
        request.setModelInvariantId("rb-name-1");
        request.setModelCustomizationId("rb-version-1");
        request.setK8sRBProfileName("profile-1");
        request.setCloudRegionId("region-1");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/instance", request, String.class);

        assertEquals(500, response.getStatusCodeValue());
    }

    /**
     * Poll WireMock until the expected callback POST arrives (max 10 seconds).
     */
    private void awaitWireMockCallback(String url) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            try {
                WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(url)));
                return;
            } catch (VerificationException e) {
                Thread.sleep(200);
            }
        }
    }
}
