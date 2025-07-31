/*
 * Copyright © 2025 Deutsche Telekom
 *
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
 */
package org.onap.so.adapters.cnf.rest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import lombok.SneakyThrows;

@Ignore
@EnableAutoConfiguration
@ImportAutoConfiguration(classes = { TraceWebAutoConfiguration.class, ZipkinAutoConfiguration.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    "spring.sleuth.enabled=true",
    "spring.sleuth.sampler.probability=1.0"
})
public class TracingTest {

  private static int wireMockPort = SocketUtils.findAvailableTcpPort();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(wireMockPort));

  @LocalServerPort
  private int port;

  RestTemplate restTemplate;

  @Before
  public void setup() {
    this.restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + port));
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.sleuth.enabled", () -> "true");
    registry.add("spring.zipkin.baseUrl", () -> "http://localhost:" + wireMockPort);
    registry.add("spring.sleuth.sampler.probability", () -> "1.0");
  }

  @Test
  @SneakyThrows
  public void thatTracesAreExported() throws InterruptedException {
    WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v2/spans"))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

    try {
      restTemplate.getForObject("http://localhost:" + port + "/foo", String.class);
    } catch (RestClientException e) {
      // this provokes a 404. For the test it's not important what is returned here
    }

    Thread.sleep(1000);
    WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/v2/spans")));
  }
}
