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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SoCallbackClientIntegrationTest {

    private static int wireMockPort = SocketUtils.findAvailableTcpPort();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(wireMockPort));

    @Autowired
    private SoCallbackClient soCallbackClient;

    @Before
    public void setUp() {
        WireMock.reset();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("multicloud.endpoint", () -> "http://localhost:9999");
        registry.add("spring.security.usercredentials[0].username", () -> "test");
        registry.add("spring.security.usercredentials[0].password", () -> "test");
        registry.add("spring.security.usercredentials[0].role", () -> "ACTUATOR");
    }

    @Test
    public void thatCallbackIsSentWithBasicAuthAndCorrectBody() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/callback"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBodyFile("callback/okResponse.txt")));

        Map<String, String> body = new HashMap<>();
        body.put("status", "COMPLETED");

        ResponseEntity<String> result = soCallbackClient.sendPostCallback(
                "http://localhost:" + wireMockPort + "/callback", body);

        assertEquals(200, result.getStatusCodeValue());
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/callback"))
                .withHeader("Content-Type", WireMock.containing("text/plain"))
                .withBasicAuth(new BasicCredentials("test", "test"))
                .withRequestBody(WireMock.containing("COMPLETED")));
    }
}
