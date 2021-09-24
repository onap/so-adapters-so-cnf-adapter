/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Orange.
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

package org.onap.so.adapters.cnf.service.healthcheck;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckInstance;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckInstanceResponse;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheckSimple;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(CnfAdapterService.class);

    private final MulticloudClient instanceApi;

    @Autowired
    public HealthCheckService(MulticloudClient multicloudClient) {
        this.instanceApi = multicloudClient;
    }

    public HealthCheckResponse healthCheck(CheckInstanceRequest healthCheckRequest) throws Exception {
        log.info("Health check - START");
        List<HealthCheckInstance> instanceHealthCheckList = startInstanceHealthCheck(healthCheckRequest);
        HealthCheckResponse statuses = getStatuses(instanceHealthCheckList);
        log.info("Health check - END");

        return statuses;
    }

    public HealthCheckResponse healthCheckError(CheckInstanceRequest healthCheckRequest, Exception e) {
        HealthCheckResponse result = new HealthCheckResponse();

        List<HealthCheckInstanceResponse> instanceHealthCheckList = new ArrayList<>();
        for (InstanceRequest instanceRequest : healthCheckRequest.getInstances()) {
            HealthCheckInstanceResponse healthCheck = new HealthCheckInstanceResponse(
                    instanceRequest.getInstanceId(), e.getMessage(), "Failed");
            instanceHealthCheckList.add(healthCheck);
        }

        result.setInstanceResponse(instanceHealthCheckList);
        return result;
    }

    private List<HealthCheckInstance> startInstanceHealthCheck(CheckInstanceRequest healthCheckRequest) throws Exception {
        log.debug("startInstanceHealthCheck - START");
        List<HealthCheckInstance> healthCheckInstanceList = new ArrayList<>();

        for (InstanceRequest instance : healthCheckRequest.getInstances()) {
            String instanceId = instance.getInstanceId();
            K8sRbInstanceHealthCheckSimple response = instanceApi.startInstanceHealthCheck(instanceId);
            log.info("K8sRbInstanceHealthCheckSimple: {}", response);
            healthCheckInstanceList.add(new HealthCheckInstance(instanceId, response.getId()));
        }

        log.info("healthCheckInstanceList: {}", healthCheckInstanceList);
        log.debug("startInstanceHealthCheck - END");
        return healthCheckInstanceList;
    }

    private HealthCheckResponse getStatuses(List<HealthCheckInstance> instanceHealthCheckList) throws Exception {
        log.debug("getStatuses - START");
        List<HealthCheckThread> threads = instanceHealthCheckList.stream()
                .map(HealthCheckThread::new)
                .collect(Collectors.toList());

        int processors = Runtime.getRuntime().availableProcessors();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Health-check-thread-%d").build();
        ExecutorService executorService = newFixedThreadPool(processors, threadFactory);
        HealthCheckResponse response = new HealthCheckResponse();
        List<HealthCheckInstanceResponse> healthCheckInstance = null;
        healthCheckInstance = executorService.invokeAll(threads).stream()
            .map(future -> {
                try {
                    InstanceStatusTuple instanceStatusTuple = future.get();
                    String instanceId = instanceStatusTuple.getInstanceId();
                    String status = instanceStatusTuple.getStatus();
                    return new HealthCheckInstanceResponse(instanceId, null, status);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
        response.setInstanceResponse(healthCheckInstance);
        log.info("Get statuses response: \n {}", response);
        log.debug("getStatuses - END");
        return response;
    }

    private class HealthCheckThread implements Callable<InstanceStatusTuple> {

        private final HealthCheckInstance healthCheckInstance;

        HealthCheckThread(HealthCheckInstance healthCheckInstance) {
            this.healthCheckInstance = healthCheckInstance;
        }

        /**
         * Approx 5 minutes thread
         * If timeout method returns tuple with HeatStackId => Timeout
         * @return InstanceStatusTuple
         * @throws InterruptedException
         * @throws BadResponseException
         */
        @Override
        public InstanceStatusTuple call() throws InterruptedException, BadResponseException {
            log.info("{} started for: {}", Thread.currentThread().getName(), healthCheckInstance);
            for (int retry = 0; retry < 30; retry++) {
                K8sRbInstanceHealthCheck response = instanceApi.getInstanceHealthCheck(healthCheckInstance.getInstanceId(), healthCheckInstance.getHealthCheckInstance());
                log.debug("Response for instanceId={}: {}", healthCheckInstance, response);
                String status = response.getStatus();
                if (!"RUNNING".equals(status.toUpperCase())) {
                    log.info("Poll status: {} for {}", status, healthCheckInstance);
                    instanceApi.deleteInstanceHealthCheck(healthCheckInstance.getInstanceId(), healthCheckInstance.getHealthCheckInstance());
                    return new InstanceStatusTuple(healthCheckInstance.getInstanceId(), status);
                }
                sleep(10_000L);
            }
            //Timeout
            instanceApi.deleteInstanceHealthCheck(healthCheckInstance.getInstanceId(), healthCheckInstance.getHealthCheckInstance());
            return new InstanceStatusTuple(healthCheckInstance.getInstanceId(), "Unknown");
        }
    }

    private class InstanceStatusTuple {
        private final String instanceId;
        private final String status;

        InstanceStatusTuple(String instanceId, String status) {
            this.instanceId = instanceId;
            this.status = status;
        }

        String getInstanceId() {
            return instanceId;
        }

        String getStatus() {
            return status;
        }
    }

}
