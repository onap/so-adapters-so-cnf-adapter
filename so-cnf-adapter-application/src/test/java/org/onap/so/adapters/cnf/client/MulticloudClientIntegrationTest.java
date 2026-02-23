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

package org.onap.so.adapters.cnf.client;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheckSimple;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionResponse;
import org.onap.so.client.exception.BadResponseException;
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
public class MulticloudClientIntegrationTest {

    private static int wireMockPort = SocketUtils.findAvailableTcpPort();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(wireMockPort));

    @Autowired
    private MulticloudClient multicloudClient;

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
    public void thatAllInstancesCanBeRetrieved() throws BadResponseException {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/allInstancesResponse.json")));

        List<InstanceResponse> instances = multicloudClient.getAllInstances();

        assertEquals(2, instances.size());
        assertEquals("inst-1", instances.get(0).getId());
        assertEquals("inst-2", instances.get(1).getId());
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/instance"))
                .withHeader("Accept", WireMock.equalTo("application/json")));
    }

    @Test
    public void thatEmptyListReturnedWhenNoDocumentsFound() throws BadResponseException {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("no documents for given ID")));

        List<InstanceResponse> instances = multicloudClient.getAllInstances();

        assertTrue(instances.isEmpty());
    }

    @Test
    public void thatInstanceStatusCanBeRetrieved() throws BadResponseException {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/instance/test-inst/status"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/instanceStatusResponse.json")));

        K8sRbInstanceStatus status = multicloudClient.getInstanceStatus("test-inst");

        assertEquals(3, status.getResourceCount());
        assertTrue(status.isReady());
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/instance/test-inst/status")));
    }

    @Test
    public void thatSubscriptionCanBeRegistered() throws BadResponseException {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/subscriptionResponse.json")));

        SubscriptionRequest request = new SubscriptionRequest();
        request.setName("test-sub");
        request.setCallbackUrl("http://callback");
        request.setMinNotifyInterval(10);

        SubscriptionResponse response = multicloudClient.registerSubscription("test-inst", request);

        assertEquals("test-sub", response.getName());
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription"))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .withRequestBody(WireMock.matchingJsonPath("$.name", WireMock.equalTo("test-sub")))
                .withRequestBody(WireMock.matchingJsonPath("$['callback-url']",
                        WireMock.equalTo("http://callback"))));
    }

    @Test
    public void thatSubscriptionExistenceCanBeChecked() throws BadResponseException {
        WireMock.stubFor(WireMock.get(
                WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription/test-sub"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/subscriptionExistsResponse.json")));

        assertTrue(multicloudClient.hasSubscription("test-inst", "test-sub"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription/test-sub")));
    }

    @Test
    public void thatHasSubscriptionReturnsFalseWhenNotFound() throws BadResponseException {
        WireMock.stubFor(WireMock.get(
                WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription/missing"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("no documents for given ID")));

        assertFalse(multicloudClient.hasSubscription("test-inst", "missing"));
    }

    @Test
    public void thatSubscriptionCanBeDeleted() throws BadResponseException {
        WireMock.stubFor(WireMock.delete(
                WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription/test-sub"))
                .willReturn(WireMock.aResponse().withStatus(200)));

        multicloudClient.deleteSubscription("test-inst", "test-sub");

        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/status/subscription/test-sub")));
    }

    @Test
    public void thatInstanceCanBeUpgraded() throws BadResponseException {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance/test-inst/upgrade"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/upgradeInstanceResponse.json")));

        MulticloudInstanceRequest upgradeRequest = new MulticloudInstanceRequest();
        upgradeRequest.setRbName("test-rb");
        upgradeRequest.setRbVersion("v1");
        upgradeRequest.setProfileName("default");
        upgradeRequest.setCloudRegion("region-1");

        String result = multicloudClient.upgradeInstance("test-inst", upgradeRequest);

        assertNotNull(result);
        assertTrue(result.contains("test-inst"));
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/upgrade"))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-name']",
                        WireMock.equalTo("test-rb")))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-version']",
                        WireMock.equalTo("v1")))
                .withRequestBody(WireMock.matchingJsonPath("$['profile-name']",
                        WireMock.equalTo("default"))));
    }

    @Test
    public void thatHealthCheckCanBeStarted() throws BadResponseException {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/instance/test-inst/healthcheck"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/startHealthCheckResponse.json")));

        K8sRbInstanceHealthCheckSimple result =
                multicloudClient.startInstanceHealthCheck("test-inst");

        assertEquals("hc-1", result.getId());
        assertEquals("RUNNING", result.getStatus());
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/healthcheck")));
    }

    @Test
    public void thatHealthCheckCanBeRetrieved() throws BadResponseException {
        WireMock.stubFor(WireMock.get(
                WireMock.urlEqualTo("/v1/instance/test-inst/healthcheck/hc-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/getHealthCheckResponse.json")));

        K8sRbInstanceHealthCheck result =
                multicloudClient.getInstanceHealthCheck("test-inst", "hc-1");

        assertEquals("test-inst", result.getInstanceId());
        assertEquals("hc-1", result.getHealthcheckId());
        assertEquals("STOPPED", result.getStatus());
    }

    @Test
    public void thatHealthCheckCanBeDeleted() throws BadResponseException {
        WireMock.stubFor(WireMock.delete(
                WireMock.urlEqualTo("/v1/instance/test-inst/healthcheck/hc-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody("")));

        multicloudClient.deleteInstanceHealthCheck("test-inst", "hc-1");

        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/instance/test-inst/healthcheck/hc-1")));
    }
}
