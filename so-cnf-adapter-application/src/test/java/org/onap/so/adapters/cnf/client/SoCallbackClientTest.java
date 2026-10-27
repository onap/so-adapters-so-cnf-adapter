/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class SoCallbackClientTest {

    private static final String CALLBACK_URL = "http://so-bpmn-infra:8081/mso/WorkflowMessage/callback";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    @Test
    public void sendPostCallback_sendsSoCredentials() {
        server.expect(requestTo(CALLBACK_URL)).andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic YnBlbDpwYXNzd29yZDEk")).andRespond(withSuccess());

        new SoCallbackClient(restTemplate, "bpel", "password1$").sendPostCallback(CALLBACK_URL, "body");

        server.verify();
    }

    @Test
    public void sendPostCallback_withoutSoCredentials_sendsNoAuthorization() {
        server.expect(requestTo(CALLBACK_URL)).andExpect(method(HttpMethod.POST))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION)).andRespond(withSuccess());

        new SoCallbackClient(restTemplate, null, null).sendPostCallback(CALLBACK_URL, "body");

        server.verify();
    }
}
