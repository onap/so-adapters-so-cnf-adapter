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
package org.onap.so.cnfm.lcm.bpmn.flows.tasks;

import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesClientProvider;
import org.springframework.stereotype.Component;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.kubernetes.client.openapi.ApiClient;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class MockedKubernetesClientProvider implements KubernetesClientProvider {

    private WireMockServer wireMockServer;

    @Override
    public ApiClient getApiClient(final String kubeConfigFile) {
        if (wireMockServer != null) {
            final ApiClient client = new ApiClient();
            client.setBasePath("http://localhost:" + wireMockServer.port());
            return client;
        }
        return null;
    }

    @Override
    public void closeApiClient(final String kubeConfigFile) {

    }

    public void setWireMockServer(final WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }



}
