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
package org.onap.so.cnfm.lcm.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpType;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobAction;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataBaseServiceIntegrationTest {

    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Test
    public void contextLoads() {
        assertNotNull(databaseServiceProvider);
    }

    @Test
    public void testJobCrud() {
        final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final Job job = new Job().jobType("CnfTest").jobAction(JobAction.CREATE)
                .resourceId(UUID.randomUUID().toString()).resourceName("test-resource")
                .status(JobStatusEnum.STARTED).startTime(now);

        databaseServiceProvider.addJob(job);

        final Optional<Job> retrieved = databaseServiceProvider.getJob(job.getJobId());
        assertTrue(retrieved.isPresent());
        assertEquals(JobAction.CREATE, retrieved.get().getJobAction());
        assertEquals("test-resource", retrieved.get().getResourceName());
    }

    @Test
    public void testAsInstCrud() {
        final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final AsInst asInst = new AsInst().name("test-as").asdId(UUID.randomUUID().toString())
                .asdInvariantId(UUID.randomUUID().toString()).asProvider("test-provider")
                .asApplicationName("test-app").asApplicationVersion("1.0")
                .serviceInstanceId(UUID.randomUUID().toString())
                .serviceInstanceName("test-service").cloudOwner("test-cloud")
                .cloudRegion("test-region").tenantId(UUID.randomUUID().toString())
                .namespace("test-ns").status(State.NOT_INSTANTIATED).statusUpdatedTime(now);

        databaseServiceProvider.saveAsInst(asInst);

        final Optional<AsInst> retrieved = databaseServiceProvider.getAsInst(asInst.getAsInstId());
        assertTrue(retrieved.isPresent());
        assertEquals("test-as", retrieved.get().getName());
        assertEquals(State.NOT_INSTANTIATED, retrieved.get().getStatus());
    }

    @Test
    public void testAsDeploymentItemWithLifecycleParam() {
        final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        final AsInst asInst = new AsInst().name("test-as-deploy").asdId(UUID.randomUUID().toString())
                .asdInvariantId(UUID.randomUUID().toString()).asProvider("provider")
                .asApplicationName("app").asApplicationVersion("1.0")
                .serviceInstanceId(UUID.randomUUID().toString())
                .serviceInstanceName("svc").cloudOwner("cloud").cloudRegion("region")
                .tenantId(UUID.randomUUID().toString()).namespace("ns")
                .status(State.NOT_INSTANTIATED).statusUpdatedTime(now);
        databaseServiceProvider.saveAsInst(asInst);

        final AsDeploymentItem item = new AsDeploymentItem().asInst(asInst)
                .name("helm-chart").releaseName("test-release").status(State.NOT_INSTANTIATED)
                .createTime(now).lastUpdateTime(now);
        final AsLifecycleParam param = new AsLifecycleParam().asDeploymentItemInst(item)
                .asLifecycleParam("override.yaml");
        item.asLifecycleParams(param);

        databaseServiceProvider.saveAsDeploymentItem(item);

        final List<AsDeploymentItem> retrieved =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInst.getAsInstId());
        assertFalse(retrieved.isEmpty());
        assertEquals("helm-chart", retrieved.get(0).getName());
        assertEquals("test-release", retrieved.get(0).getReleaseName());
    }

    @Test
    public void testAsLcmOpOccCrud() {
        final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        final AsInst asInst = new AsInst().name("test-as-occ").asdId(UUID.randomUUID().toString())
                .asdInvariantId(UUID.randomUUID().toString()).asProvider("provider")
                .asApplicationName("app").asApplicationVersion("1.0")
                .serviceInstanceId(UUID.randomUUID().toString())
                .serviceInstanceName("svc").cloudOwner("cloud").cloudRegion("region")
                .tenantId(UUID.randomUUID().toString()).namespace("ns")
                .status(State.NOT_INSTANTIATED).statusUpdatedTime(now);
        databaseServiceProvider.saveAsInst(asInst);

        final AsLcmOpOcc opOcc = new AsLcmOpOcc().asInst(asInst)
                .operationState(OperationStateEnum.PROCESSING)
                .stateEnteredTime(now).startTime(now)
                .operation(AsLcmOpType.INSTANTIATE)
                .isAutoInvocation(false).operationParams("{}")
                .isCancelPending(false);

        databaseServiceProvider.addAsLcmOpOcc(opOcc);

        final Optional<AsLcmOpOcc> retrieved = databaseServiceProvider.getAsLcmOpOcc(opOcc.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(OperationStateEnum.PROCESSING, retrieved.get().getOperationState());
    }
}
