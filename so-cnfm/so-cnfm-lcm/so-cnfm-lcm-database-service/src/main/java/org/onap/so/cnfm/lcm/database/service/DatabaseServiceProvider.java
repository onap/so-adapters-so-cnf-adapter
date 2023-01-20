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

import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.database.repository.AsDeploymentItemRepository;
import org.onap.so.cnfm.lcm.database.repository.AsInstRepository;
import org.onap.so.cnfm.lcm.database.repository.AsLcmOpOccRepository;
import org.onap.so.cnfm.lcm.database.repository.AsLifecycleParamRepository;
import org.onap.so.cnfm.lcm.database.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */

@Service
public class DatabaseServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceProvider.class);

    private final JobRepository jobRepository;

    private final AsInstRepository asInstRepository;

    private final AsDeploymentItemRepository asdeploymentItemRepository;

    private final AsLifecycleParamRepository aslifecyleParamRepository;

    private final AsLcmOpOccRepository asLcmOpOccRepository;

    @Autowired
    public DatabaseServiceProvider(final JobRepository jobRepository, final AsInstRepository asInstRepository,
            final AsDeploymentItemRepository asdeploymentItemRepository,
            final AsLifecycleParamRepository aslifecyleParamRepository,
            final AsLcmOpOccRepository asLcmOpOccRepository) {
        this.jobRepository = jobRepository;
        this.asInstRepository = asInstRepository;
        this.asdeploymentItemRepository = asdeploymentItemRepository;
        this.aslifecyleParamRepository = aslifecyleParamRepository;
        this.asLcmOpOccRepository = asLcmOpOccRepository;
    }

    public boolean addJob(final Job job) {
        logger.info("Adding Job: {} to database", job);
        return jobRepository.saveAndFlush(job) != null;
    }

    public Optional<Job> getJob(final String jobId) {
        logger.info("Querying database for Job using jobId: {}", jobId);
        return jobRepository.findById(jobId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public Optional<Job> getRefreshedJob(final String jobId) {
        logger.info("Querying database for Job using jobId: {}", jobId);
        final Optional<Job> optional = getJob(jobId);
        if (optional.isPresent()) {
            jobRepository.refreshEntity(optional.get());
        }
        return optional;
    }

    public Optional<Job> getJobByResourceId(final String resourceId) {
        logger.info("Querying database for Job using resourceId: {}", resourceId);
        return jobRepository.findByResourceId(resourceId);
    }

    public boolean isAsInstExists(final String name) {
        logger.info("Checking if AsInst entry exists in database using name: {}", name);
        return asInstRepository.existsAsInstByName(name);
    }

    public boolean saveAsInst(final AsInst asInstance) {
        logger.info("Saving AsInst: {} to database", asInstance);
        return asInstRepository.saveAndFlush(asInstance) != null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateAsInstState(final String asInstId, final State state) {
        logger.info("Updating AsInst: {} State to {}", asInstId, state);
        return asInstRepository.updateAsInstState(asInstId, state) > 0;
    }

    public Optional<AsInst> getAsInst(final String asInstId) {
        logger.info("Querying database for AsInst using nsInstId: {}", asInstId);
        return asInstRepository.findById(asInstId);
    }

    public Optional<AsInst> getAsInstByName(final String name) {
        logger.info("Querying database for AsInst using name: {}", name);
        return asInstRepository.findByName(name);
    }

    public void deleteAsInst(final String asInstId) {
        logger.info("Deleting AsInst with asInstId: {}", asInstId);
        asInstRepository.deleteById(asInstId);
    }

    public boolean saveAsDeploymentItem(final AsDeploymentItem asdeploymentItem) {
        logger.info("Saving AsDeploymentItem: {} to database", asdeploymentItem);
        return asdeploymentItemRepository.saveAndFlush(asdeploymentItem) != null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateAsDeploymentItemState(final String asDeploymentItemInstId, final State state) {
        logger.info("Updating AsDeploymentItem: {} State to {}", asDeploymentItemInstId, state);
        return asdeploymentItemRepository.updateAsDeploymentItemState(asDeploymentItemInstId, state) > 0;
    }

    public List<AsDeploymentItem> getAsDeploymentItemByAsInstId(final String asInstId) {
        logger.info("Querying database for AsdeploymentItem using asInstId: {}", asInstId);
        return asdeploymentItemRepository.findByAsInstAsInstId(asInstId);
    }

    public List<AsDeploymentItem> getAsDeploymentItemByAsInstIdAndName(final String asInstId, final String name) {
        logger.info("Querying database for AsDeploymentItem using asInstId: {} and name : {} ", asInstId, name);
        return asdeploymentItemRepository.findByAsInstAsInstIdAndName(asInstId, name);
    }

    public Optional<AsDeploymentItem> getAsDeploymentItem(final String asDeploymentItemInstId) {
        logger.info("Querying database for AsdeploymentItem using asDeploymentItemInstId: {}", asDeploymentItemInstId);
        return asdeploymentItemRepository.findByAsDeploymentItemInstId(asDeploymentItemInstId);
    }

    public boolean isAsDeploymentItemExists(final String asDeploymentItemInstId) {
        logger.info("Checking if AsdeploymentItem entry exists in database using asDeploymentItemInstId: {}",
                asDeploymentItemInstId);
        return asdeploymentItemRepository.findByAsDeploymentItemInstId(asDeploymentItemInstId).isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAsDeploymentItem(final String asInstId) {
        logger.info("Deleting AsdeploymentItem with asInstId: {} from database", asInstId);
        asdeploymentItemRepository.deleteAsDeploymentItemUsingAsInstId(asInstId);
    }

    public boolean saveAsLifecycleParam(final AsLifecycleParam aslifecyleparam) {
        logger.info("Saving AsLifecycleParam: {} to database", aslifecyleparam);
        return aslifecyleParamRepository.saveAndFlush(aslifecyleparam) != null;
    }

    public List<AsLifecycleParam> getAsLifecycleParamByAsDeploymentItemId(final String asDeploymentItemId) {
        logger.info("Querying database for Aslifecycleparam using asDeploymentItemId: {}", asDeploymentItemId);
        // NEXT
        return aslifecyleParamRepository.findByAsDeploymentItemInstAsDeploymentItemInstId(asDeploymentItemId);
    }

    public Optional<AsLifecycleParam> getAsLifecycleParam(final Integer asLifecycleParamId) {
        logger.info("Querying database for Aslifecycleparam using AslifecycleParamId: {}", asLifecycleParamId);
        return aslifecyleParamRepository.findByAsLifecycleParamId(asLifecycleParamId);
    }

    public boolean addAsLcmOpOcc(final AsLcmOpOcc asLcmOpOcc) {
        logger.info("Adding AsLcmOpOcc: {} to database", asLcmOpOcc);
        return asLcmOpOccRepository.saveAndFlush(asLcmOpOcc) != null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateAsLcmOpOccOperationState(final String id, final OperationStateEnum operationState) {
        logger.info("Updating AsLcmOpOcc: {} operationState to {}", id, operationState);
        return asLcmOpOccRepository.updateAsLcmOpOccOperationState(id, operationState) > 0;
    }

    public Optional<AsLcmOpOcc> getAsLcmOpOcc(final String id) {
        logger.info("Querying database for AsLcmOpOcc using id: {}", id);
        return asLcmOpOccRepository.findById(id);
    }
}
