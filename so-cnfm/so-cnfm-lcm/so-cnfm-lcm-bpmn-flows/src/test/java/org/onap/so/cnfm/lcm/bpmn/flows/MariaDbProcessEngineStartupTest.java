/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG.
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
package org.onap.so.cnfm.lcm.bpmn.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.Test;
import org.onap.so.cnfm.lcm.bpmn.flows.tasks.MockedHelmClientConfiguration;
import org.onap.so.cnfm.lcm.bpmn.flows.tasks.MockedKubernetesClientProviderConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles({"test", "mariadb"})
@AutoConfigureWireMock(port = 0)
@Import({MockedHelmClientConfiguration.class, MockedKubernetesClientProviderConfiguration.class})
class MariaDbProcessEngineStartupTest {

    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11.7").withDatabaseName("camundabpmn")
            .withUsername("test_user").withPassword("test_password");

    @DynamicPropertySource
    static void configureDatabase(final DynamicPropertyRegistry registry) {
        registry.add("DB_HOST", MARIADB::getHost);
        registry.add("DB_PORT", MARIADB::getFirstMappedPort);
        registry.add("DB_USERNAME", MARIADB::getUsername);
        registry.add("DB_PASSWORD", MARIADB::getPassword);
    }

    @Autowired
    private ProcessEngine processEngine;

    @Test
    void processEngineStartsOnMariaDbWithDefaultIsolationLevel() {
        assertNotNull(processEngine);
        assertEquals(0, processEngine.getRuntimeService().createProcessInstanceQuery().count());
    }
}
