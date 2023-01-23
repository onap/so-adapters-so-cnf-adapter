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
package org.onap.so.cnfm.lcm.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.cnfm.lcm.database.TestApplication;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpType;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobAction;
import org.onap.so.cnfm.lcm.database.beans.JobStatus;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DatabaseServiceProviderTest {

    private static final String RANDOM_ID = UUID.randomUUID().toString();
    private static final String DUMMY_NAME = "NAME";
    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Test
    public void testAddJob_StoredInDatabase() {
        final Job expected = new Job().jobType("TYPE").jobAction(JobAction.CREATE).resourceId(RANDOM_ID)
                .resourceName(DUMMY_NAME).startTime(CURRENT_DATE_TIME).status(JobStatusEnum.STARTED);
        databaseServiceProvider.addJob(expected);

        Optional<Job> actual = databaseServiceProvider.getJob(expected.getJobId());
        assertEquals(expected, actual.get());

        actual = databaseServiceProvider.getRefreshedJob(expected.getJobId());
        assertEquals(expected, actual.get());

    }

    @Test
    public void testAddJobWithJobStatus_StoredInDatabase() {
        final Job job = new Job().jobType("TYPE").jobAction(JobAction.CREATE).resourceId(RANDOM_ID)
                .resourceName(DUMMY_NAME).startTime(CURRENT_DATE_TIME).status(JobStatusEnum.STARTED);
        databaseServiceProvider.addJob(job);

        final JobStatus jobStatus = new JobStatus().status(JobStatusEnum.STARTED)
                .description("Create AS workflow process started").updatedTime(CURRENT_DATE_TIME);
        databaseServiceProvider.addJob(job.jobStatus(jobStatus));

        final Optional<Job> actual = databaseServiceProvider.getJob(job.getJobId());
        final Job actualJob = actual.get();

        assertEquals(job.getJobId(), actualJob.getJobId());
        assertFalse(actualJob.getJobStatuses().isEmpty());
        assertEquals(job.getJobId(), actualJob.getJobStatuses().get(0).getJob().getJobId());

    }

    @Test
    public void testAddAsInst_StoredInDatabase_ableTofindByQuery() {

        final String name = DUMMY_NAME + UUID.randomUUID().toString();
        final AsInst asInst = new AsInst().name(name).asdId(RANDOM_ID).status(State.NOT_INSTANTIATED)
                .asdInvariantId(RANDOM_ID).statusUpdatedTime(CURRENT_DATE_TIME).asApplicationName("asApplicationName")
                .asApplicationVersion("asApplicationVersion").asProvider("asProvider").serviceInstanceId(RANDOM_ID)
                .serviceInstanceName("serviceInstanceName").cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId");

        databaseServiceProvider.saveAsInst(asInst);

        Optional<AsInst> actual = databaseServiceProvider.getAsInst(asInst.getAsInstId());
        AsInst actualAsInst = actual.get();
        assertEquals(asInst.getAsInstId(), actualAsInst.getAsInstId());
        assertEquals(RANDOM_ID, actualAsInst.getAsdId());
        assertEquals(State.NOT_INSTANTIATED, actualAsInst.getStatus());
        assertEquals(RANDOM_ID, actualAsInst.getAsdInvariantId());
        assertEquals(CURRENT_DATE_TIME, actualAsInst.getStatusUpdatedTime());

        actual = databaseServiceProvider.getAsInstByName(name);
        actualAsInst = actual.get();

        assertEquals(asInst.getAsInstId(), actualAsInst.getAsInstId());
        assertEquals(RANDOM_ID, actualAsInst.getAsdId());
        assertEquals(State.NOT_INSTANTIATED, actualAsInst.getStatus());
        assertEquals(RANDOM_ID, actualAsInst.getAsdInvariantId());
        assertEquals(CURRENT_DATE_TIME, actualAsInst.getStatusUpdatedTime());


        assertTrue(databaseServiceProvider.isAsInstExists(name));
    }

    @Test
    public void testAddAsdeploymentItem_StoredInDatabase_ableTofindByQuery() {

        final String name = DUMMY_NAME + UUID.randomUUID().toString();
        final AsInst asInst = new AsInst().name(name).asdId(RANDOM_ID).status(State.NOT_INSTANTIATED)
                .asdInvariantId(RANDOM_ID).statusUpdatedTime(CURRENT_DATE_TIME).asApplicationName("asApplicationName")
                .asApplicationVersion("asApplicationVersion").asProvider("asProvider").serviceInstanceId(RANDOM_ID)
                .serviceInstanceName("serviceInstanceName").cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId");

        databaseServiceProvider.saveAsInst(asInst);

        final AsDeploymentItem asdeploymentItem =
                new AsDeploymentItem().asInst(asInst).name(DUMMY_NAME).itemId(RANDOM_ID).status(State.NOT_INSTANTIATED)
                        .createTime(CURRENT_DATE_TIME).lastUpdateTime(CURRENT_DATE_TIME).releaseName("test");
        databaseServiceProvider.saveAsDeploymentItem(asdeploymentItem);

        final Optional<AsDeploymentItem> actual =
                databaseServiceProvider.getAsDeploymentItem(asdeploymentItem.getAsDeploymentItemInstId());
        final AsDeploymentItem actualAsdeploymentItem = actual.get();
        assertEquals(asInst.getAsInstId(), actualAsdeploymentItem.getAsInst().getAsInstId());
        assertEquals(asdeploymentItem.getAsDeploymentItemInstId(), actualAsdeploymentItem.getAsDeploymentItemInstId());
        assertEquals(asdeploymentItem.getName(), actualAsdeploymentItem.getName());
        assertEquals(asdeploymentItem.getItemId(), actualAsdeploymentItem.getItemId());
        assertEquals(asdeploymentItem.getStatus(), actualAsdeploymentItem.getStatus());
        assertEquals(asdeploymentItem.getCreateTime(), actualAsdeploymentItem.getCreateTime());
        assertEquals(asdeploymentItem.getLastUpdateTime(), actualAsdeploymentItem.getLastUpdateTime());
        assertEquals(asdeploymentItem.getReleaseName(), actualAsdeploymentItem.getReleaseName());


        List<AsDeploymentItem> asdeploymentItemList =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(asInst.getAsInstId());
        assertFalse(asdeploymentItemList.isEmpty());
        assertEquals(asInst.getAsInstId(), asdeploymentItemList.get(0).getAsInst().getAsInstId());

        asdeploymentItemList =
                databaseServiceProvider.getAsDeploymentItemByAsInstIdAndName(asInst.getAsInstId(), DUMMY_NAME);

        assertFalse(asdeploymentItemList.isEmpty());
        assertEquals(asInst.getAsInstId(), asdeploymentItemList.get(0).getAsInst().getAsInstId());
        assertEquals(DUMMY_NAME, asdeploymentItemList.get(0).getName());

        final AsLifecycleParam aslifecycleparam =
                new AsLifecycleParam().asDeploymentItemInst(asdeploymentItem).asLifecycleParam("TEST");
        databaseServiceProvider.saveAsLifecycleParam(aslifecycleparam);

        final Optional<AsLifecycleParam> actualLP =
                databaseServiceProvider.getAsLifecycleParam(aslifecycleparam.getAsLifecycleParamId());
        final AsLifecycleParam actualLifecycleParam = actualLP.get();
        assertEquals(aslifecycleparam.getLifecycleParam(), actualLifecycleParam.getLifecycleParam());
        assertEquals(asdeploymentItem.getAsDeploymentItemInstId(), actualLifecycleParam.getAsDeploymentItemInst().getAsDeploymentItemInstId());
        assertEquals(aslifecycleparam.getAsLifecycleParamId(), actualLifecycleParam.getAsLifecycleParamId());

        List<AsLifecycleParam> aslifecycleParamsList =
                databaseServiceProvider.getAsLifecycleParamByAsDeploymentItemId(asdeploymentItem.getAsDeploymentItemInstId());
        assertFalse(aslifecycleParamsList.isEmpty());
        assertEquals(asdeploymentItem.getAsDeploymentItemInstId(), aslifecycleParamsList.get(0).getAsDeploymentItemInst().getAsDeploymentItemInstId());
    }

    @Test
    public void testAddAsLcmOpOcc_StoredInDatabase_ableTofindByQuery() {
        final String name = DUMMY_NAME + UUID.randomUUID().toString();
        final AsInst asInst = new AsInst().name(name).asdId(RANDOM_ID).status(State.NOT_INSTANTIATED)
                .asdInvariantId(RANDOM_ID).statusUpdatedTime(CURRENT_DATE_TIME).asApplicationName("asApplicationName")
                .asApplicationVersion("asApplicationVersion").asProvider("asProvider").serviceInstanceId(RANDOM_ID)
                .serviceInstanceName("serviceInstanceName").cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId");

        databaseServiceProvider.saveAsInst(asInst);

        final AsLcmOpOcc asLcmOpOcc = new AsLcmOpOcc().asInst(asInst).operationState(OperationStateEnum.PROCESSING)
                .isCancelPending(false).isAutoInvocation(false).operation(AsLcmOpType.INSTANTIATE)
                .startTime(CURRENT_DATE_TIME).stateEnteredTime(CURRENT_DATE_TIME).operationParams("");


        databaseServiceProvider.addAsLcmOpOcc(asLcmOpOcc);

        final Optional<AsLcmOpOcc> actual = databaseServiceProvider.getAsLcmOpOcc(asLcmOpOcc.getId());
        final AsLcmOpOcc actualLcmOpOcc = actual.get();
        assertEquals(asLcmOpOcc.getId(), actualLcmOpOcc.getId());

        assertEquals(asInst.getAsInstId(), actualLcmOpOcc.getAsInst().getAsInstId());

    }
}
