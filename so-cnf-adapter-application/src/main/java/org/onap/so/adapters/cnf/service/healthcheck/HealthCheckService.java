package org.onap.so.adapters.cnf.service.healthcheck;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.*;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
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
                    String reason = null;
                    return new HealthCheckInstanceResponse(instanceId, reason, status);
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
            return new InstanceStatusTuple(healthCheckInstance.getInstanceId(), "Timeout");
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
