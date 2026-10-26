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
package org.onap.so.cnfm.lcm.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobAction;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("mariadb")
class MariaDbLiquibaseMigrationTest {

    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11.7").withDatabaseName("cnfm")
            .withUsername("test_user").withPassword("test_password");

    @DynamicPropertySource
    static void configureDatabase(final DynamicPropertyRegistry registry) {
        registry.add("DB_HOST", MARIADB::getHost);
        registry.add("DB_PORT", MARIADB::getFirstMappedPort);
        registry.add("DB_USERNAME", MARIADB::getUsername);
        registry.add("DB_PASSWORD", MARIADB::getPassword);
    }

    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Test
    void jobIsPersistedInUpperCaseTableOnMariaDb() {
        final Job job = new Job().jobType("TYPE").jobAction(JobAction.CREATE)
                .resourceId(UUID.randomUUID().toString()).resourceName("test-resource")
                .startTime(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)).status(JobStatusEnum.STARTED);

        databaseServiceProvider.addJob(job);

        final Optional<Job> retrieved = databaseServiceProvider.getJob(job.getJobId());
        assertTrue(retrieved.isPresent(), "Job should be persisted in MariaDB");
        assertEquals(job.getJobId(), retrieved.get().getJobId());
    }
}
