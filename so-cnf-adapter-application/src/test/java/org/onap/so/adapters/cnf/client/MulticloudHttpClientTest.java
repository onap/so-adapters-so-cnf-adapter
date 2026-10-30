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

package org.onap.so.adapters.cnf.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;

public class MulticloudHttpClientTest {

    private static final String BASE_URL = "http://multicloud-k8s:9015";
    private static final String BODY = "{\"result\":\"ok\"}";

    private MockRestServiceServer server;
    private MulticloudHttpClient client;

    @Before
    public void setUp() {
        MulticloudConfiguration configuration = mock(MulticloudConfiguration.class);
        when(configuration.getMulticloudUrl()).thenReturn(BASE_URL);
        MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
        client = new MulticloudHttpClient(configuration, new RestTemplateBuilder(customizer));
        server = customizer.getServer();
    }

    @After
    public void tearDown() {
        server.verify();
    }

    @Test
    public void thatGetReturnsResponseBody() {
        server.expect(requestTo(BASE_URL + "/v1/rb/definition/rb/v1")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(BODY, MediaType.APPLICATION_JSON));

        assertEquals(BODY, client.get("/v1/rb/definition/rb/v1"));
    }

    @Test
    public void thatGetReturnsErrorBodyInsteadOfThrowing() {
        server.expect(requestTo(BASE_URL + "/v1/rb/definition/missing")).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("rb not found").contentType(MediaType.TEXT_PLAIN));

        assertEquals("rb not found", client.get("/v1/rb/definition/missing"));
    }

    @Test
    public void thatDeleteReturnsResponseBody() {
        server.expect(requestTo(BASE_URL + "/v1/connectivity-info/conn-1")).andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess(BODY, MediaType.APPLICATION_JSON));

        assertEquals(BODY, client.delete("/v1/connectivity-info/conn-1"));
    }

    @Test
    public void thatDeleteWithoutResponseBodyReturnsNull() {
        server.expect(requestTo(BASE_URL + "/v1/connectivity-info/conn-1")).andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        assertNull(client.delete("/v1/connectivity-info/conn-1"));
    }

    @Test
    public void thatPostSendsBodyAsJson() throws IOException {
        server.expect(requestTo(BASE_URL + "/v1/connectivity-info")).andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$['cloud-region']").value("region-1"))
                .andRespond(withSuccess(BODY, MediaType.APPLICATION_JSON));

        assertEquals(BODY, client.post("/v1/connectivity-info", connectivityInfo()));
    }

    @Test
    public void thatPostReturnsErrorBodyInsteadOfThrowing() throws IOException {
        server.expect(requestTo(BASE_URL + "/v1/connectivity-info")).andExpect(method(HttpMethod.POST))
                .andRespond(withServerError().body("internal error"));

        assertEquals("internal error", client.post("/v1/connectivity-info", connectivityInfo()));
    }

    @Test
    public void thatPutSendsBodyAsJson() throws IOException {
        server.expect(requestTo(BASE_URL + "/v1/definition/rb/v1/profile/p/config/c"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$['cloud-owner']").value("owner-1"))
                .andRespond(withSuccess(BODY, MediaType.APPLICATION_JSON));

        assertEquals(BODY, client.put("/v1/definition/rb/v1/profile/p/config/c", connectivityInfo()));
    }

    @Test
    public void thatPutReturnsErrorBodyInsteadOfThrowing() throws IOException {
        server.expect(requestTo(BASE_URL + "/v1/definition/rb/v1/profile/p/config/c"))
                .andExpect(method(HttpMethod.PUT)).andRespond(withBadRequest().body("bad request"));

        assertEquals("bad request", client.put("/v1/definition/rb/v1/profile/p/config/c", connectivityInfo()));
    }

    @Test
    public void thatUploadSendsFileAsMultipartPart() {
        byte[] content = "chart-archive".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "chart.tgz", "application/gzip", content);
        server.expect(requestTo(BASE_URL + "/v1/rb/definition/rb/v1/content")).andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", Matchers.startsWith("multipart/form-data;boundary=")))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("name=\"file\"; filename=\"chart.tgz\""),
                        Matchers.containsString("Content-Type: application/octet-stream"),
                        Matchers.containsString("chart-archive"))))
                .andRespond(withSuccess());

        assertNull(client.uploadMultipartFile("/v1/rb/definition/rb/v1/content", file));
    }

    @Test
    public void thatUploadReturnsErrorBodyInsteadOfThrowing() {
        MockMultipartFile file = new MockMultipartFile("file", "chart.tgz", "application/gzip", new byte[] {1});
        server.expect(requestTo(BASE_URL + "/v1/rb/definition/rb/v1/content")).andExpect(method(HttpMethod.POST))
                .andRespond(withServerError().body("Error untarring"));

        assertEquals("Error untarring", client.uploadMultipartFile("/v1/rb/definition/rb/v1/content", file));
    }

    @Test
    public void thatConnectionFailureIsThrown() {
        server.expect(requestTo(BASE_URL + "/v1/rb/definition")).andRespond(withException(new IOException("refused")));

        assertThrows(ResourceAccessException.class, () -> client.get("/v1/rb/definition"));
    }

    private static ConnectivityInfo connectivityInfo() {
        ConnectivityInfo info = new ConnectivityInfo();
        info.setCloudRegion("region-1");
        info.setCloudOwner("owner-1");
        return info;
    }
}
