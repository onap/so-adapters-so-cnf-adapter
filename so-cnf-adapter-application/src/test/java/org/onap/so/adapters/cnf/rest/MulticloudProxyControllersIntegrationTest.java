/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.ConfigTemplateEntity;
import org.onap.so.adapters.cnf.model.ConfigurationEntity;
import org.onap.so.adapters.cnf.model.ConfigurationRollbackEntity;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
import org.onap.so.adapters.cnf.model.ProfileEntity;
import org.onap.so.adapters.cnf.model.ResourceBundleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class MulticloudProxyControllersIntegrationTest {

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
    public void thatConfigTemplateCanBeCreated() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(
                "/v1/rb/definition/test-rb/v1/config-template"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/configTemplateResponse.json")));

        ConfigTemplateEntity entity = new ConfigTemplateEntity();
        entity.setTemplateName("test-template");
        entity.setDescription("test config template");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template",
                entity, String.class, "test-rb", "v1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-template"));
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1/config-template"))
                .withRequestBody(WireMock.matchingJsonPath("$['template-name']",
                        WireMock.equalTo("test-template"))));
    }

    @Test
    public void thatConfigTemplateCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(
                "/v1/rb/definition/test-rb/v1/config-template/tpl-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/configTemplateResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}",
                String.class, "test-rb", "v1", "tpl-1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-template"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1/config-template/tpl-1")));
    }

    @Test
    public void thatConfigTemplateCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo(
                "/v1/rb/definition/test-rb/v1/config-template/tpl-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}",
                HttpMethod.DELETE, null, String.class, "test-rb", "v1", "tpl-1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1/config-template/tpl-1")));
    }

    @Test
    public void thatConfigurationCanBeCreated() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(
                "/v1/definition/test-rb/v1/profile/test-profile/config"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/configurationResponse.json")));

        ConfigurationEntity entity = new ConfigurationEntity();
        entity.setTemplateName("test-template");
        entity.setConfigName("test-config");
        entity.setDescription("test configuration");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config",
                entity, String.class, "test-rb", "v1", "test-profile");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-config"));
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/definition/test-rb/v1/profile/test-profile/config"))
                .withRequestBody(WireMock.matchingJsonPath("$['template-name']",
                        WireMock.equalTo("test-template")))
                .withRequestBody(WireMock.matchingJsonPath("$['config-name']",
                        WireMock.equalTo("test-config"))));
    }

    @Test
    public void thatConfigurationCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(
                "/v1/definition/test-rb/v1/profile/test-profile/config/cfg-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/configurationResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}",
                String.class, "test-rb", "v1", "test-profile", "cfg-1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-config"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/definition/test-rb/v1/profile/test-profile/config/cfg-1")));
    }

    @Test
    public void thatConfigurationCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo(
                "/v1/definition/test-rb/v1/profile/test-profile/config/cfg-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}",
                HttpMethod.DELETE, null, String.class, "test-rb", "v1", "test-profile", "cfg-1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/definition/test-rb/v1/profile/test-profile/config/cfg-1")));
    }

    @Test
    public void thatConfigurationCanBeUpdated() {
        WireMock.stubFor(WireMock.put(WireMock.urlEqualTo(
                "/v1/definition/test-rb/v1/profile/test-profile/config/cfg-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/configurationResponse.json")));

        ConfigurationEntity entity = new ConfigurationEntity();
        entity.setTemplateName("test-template");
        entity.setConfigName("test-config");
        Map<String, Object> values = new HashMap<>();
        values.put("key1", "value1");
        entity.setValues(values);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}",
                HttpMethod.PUT, new HttpEntity<>(entity), String.class,
                "test-rb", "v1", "test-profile", "cfg-1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.putRequestedFor(
                WireMock.urlEqualTo("/v1/definition/test-rb/v1/profile/test-profile/config/cfg-1"))
                .withRequestBody(WireMock.matchingJsonPath("$['template-name']",
                        WireMock.equalTo("test-template"))));
    }

    @Test
    public void thatConfigurationCanBeRolledBack() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(
                "/v1/definition/test-rb/v1/profile/test-profile/config/rollback"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/rollbackResponse.json")));

        ConfigurationRollbackEntity rollbackEntity = new ConfigurationRollbackEntity();
        rollbackEntity.setAnyOf(Collections.emptyList());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/definition/{rbName}/{rbVersion}/profile/{prName}/config/rollback",
                HttpMethod.DELETE, new HttpEntity<>(rollbackEntity), String.class,
                "test-rb", "v1", "test-profile");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/definition/test-rb/v1/profile/test-profile/config/rollback")));
    }

    @Test
    public void thatConnectivityInfoCanBeCreated() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/connectivity-info"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/connectivityInfoResponse.json")));

        ConnectivityInfo info = new ConnectivityInfo();
        info.setCloudRegion("test-region");
        info.setCloudOwner("test-owner");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/connectivity-info", info, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-region"));
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/connectivity-info"))
                .withRequestBody(WireMock.matchingJsonPath("$['cloud-region']",
                        WireMock.equalTo("test-region")))
                .withRequestBody(WireMock.matchingJsonPath("$['cloud-owner']",
                        WireMock.equalTo("test-owner"))));
    }

    @Test
    public void thatConnectivityInfoCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/connectivity-info/conn-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/connectivityInfoResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/connectivity-info/{connname}", String.class, "conn-1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-region"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/connectivity-info/conn-1")));
    }

    @Test
    public void thatConnectivityInfoCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/v1/connectivity-info/conn-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/connectivity-info/{connname}",
                HttpMethod.DELETE, null, String.class, "conn-1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/connectivity-info/conn-1")));
    }

    @Test
    public void thatProfileCanBeCreated() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(
                "/v1/rb/definition/test-rb/v1/profile"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/profileResponse.json")));

        ProfileEntity entity = new ProfileEntity();
        entity.setRbName("test-rb");
        entity.setRbVersion("v1");
        entity.setProfileName("test-profile");
        entity.setNameSpace("default");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile",
                entity, String.class, "test-rb", "v1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-profile"));
        WireMock.verify(WireMock.postRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1/profile"))
                .withRequestBody(WireMock.matchingJsonPath("$['profile-name']",
                        WireMock.equalTo("test-profile"))));
    }

    @Test
    public void thatProfileCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(
                "/v1/rb/definition/test-rb/v1/profile/prof-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/profileResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}",
                String.class, "test-rb", "v1", "prof-1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-profile"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1/profile/prof-1")));
    }

    @Test
    public void thatProfileCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo(
                "/v1/rb/definition/test-rb/v1/profile/prof-1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}",
                HttpMethod.DELETE, null, String.class, "test-rb", "v1", "prof-1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1/profile/prof-1")));
    }

    @Test
    public void thatResourceBundleCanBeCreated() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/rb/definition"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/resourceBundleResponse.json")));

        ResourceBundleEntity entity = new ResourceBundleEntity();
        entity.setRbName("test-rb");
        entity.setRbVersion("v1");
        entity.setChartName("test-chart");
        entity.setDescription("test resource bundle");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cnf-adapter/v1/rb/definition", entity, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-rb"));
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/rb/definition"))
                .withRequestBody(WireMock.matchingJsonPath("$['rb-name']",
                        WireMock.equalTo("test-rb")))
                .withRequestBody(WireMock.matchingJsonPath("$['chart-name']",
                        WireMock.equalTo("test-chart"))));
    }

    @Test
    public void thatResourceBundleCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/resourceBundleResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}",
                String.class, "test-rb", "v1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-rb"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1")));
    }

    @Test
    public void thatResourceBundleCanBeDeleted() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/emptyResponse.json")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}",
                HttpMethod.DELETE, null, String.class, "test-rb", "v1");

        assertEquals(200, response.getStatusCodeValue());
        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb/v1")));
    }

    @Test
    public void thatResourceBundleListCanBeRetrievedByName() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/rb/definition/test-rb"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/resourceBundleListResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/rb/definition/{rb-name}", String.class, "test-rb");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-rb"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition/test-rb")));
    }

    @Test
    public void thatAllResourceBundlesCanBeRetrieved() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/rb/definition"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("multicloud/resourceBundleListResponse.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/cnf-adapter/v1/rb/definition", String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("test-rb"));
        WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlEqualTo("/v1/rb/definition")));
    }
}
