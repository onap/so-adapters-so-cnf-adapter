package org.onap.so.adapters.cnf.service.healthcheck;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.halthcheck.HealthCheckInstance;
import org.onap.so.adapters.cnf.model.halthcheck.HealthCheckRequest;
import org.onap.so.adapters.cnf.model.halthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.halthcheck.K8sRbInstanceHealthCheckSimple;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
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

    public Map<String, String> healthCheck(HealthCheckRequest healthCheckRequest) {
        log.info("Health check - START");

        List<HealthCheckInstance> instanceHealthCheckList = startInstanceHealthCheck(healthCheckRequest);
        Map<String, String> statuses = getStatuses(instanceHealthCheckList);
        log.info("Health check - END");

        return statuses;
    }

    private List<HealthCheckInstance> startInstanceHealthCheck(HealthCheckRequest healthCheckRequest) {
        log.debug("startInstanceHealthCheck - START");
        List<HealthCheckInstance> healthCheckInstanceList = new ArrayList<>();

        healthCheckRequest.getInstances().forEach(instance -> {
            String instanceId = instance.getInstanceId();
            K8sRbInstanceHealthCheckSimple response = instanceApi.startInstanceHealthCheck(instanceId);
            log.info("K8sRbInstanceHealthCheckSimple: {}", response);
            healthCheckInstanceList.add(new HealthCheckInstance(instanceId, response.getId()));
        });

        log.info("healthCheckInstanceList: {}", healthCheckInstanceList);
        log.debug("startInstanceHealthCheck - END");
        return healthCheckInstanceList;
    }

    private Map<String, String> getStatuses(List<HealthCheckInstance> instanceHealthCheckList) {
        log.debug("getStatuses - START");
        List<HealthCheckThread> threads = instanceHealthCheckList.stream()
                .map(HealthCheckThread::new)
                .collect(Collectors.toList());

        int processors = Runtime.getRuntime().availableProcessors();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Health-check-thread-%d").build();
        ExecutorService executorService = newFixedThreadPool(processors, threadFactory);
        Map<String, String> statuses = null;
        try {
            statuses = executorService.invokeAll(threads).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .collect(Collectors.toMap(InstanceStatusTuple::getHealthCheckInstance, InstanceStatusTuple::getStatus));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Get statuses finished: \n {}", statuses);
        log.debug("getStatuses - END");
        return statuses;
    }

    private class HealthCheckThread implements Callable<InstanceStatusTuple> {

        private final HealthCheckInstance healthCheckInstance;

        HealthCheckThread(HealthCheckInstance healthCheckInstance) {
            this.healthCheckInstance = healthCheckInstance;
        }

        /**
         * Aprox 5 minutes thread
         * If timeout method returns tuple with HeatStackId => Timeout
         * @return InstanceStatusTuple
         * @throws InterruptedException
         */
        @Override
        public InstanceStatusTuple call() throws InterruptedException {
            log.info("{} started for: {}", Thread.currentThread().getName(), healthCheckInstance);
            for (int retry = 0; retry < 30; retry++) {
                K8sRbInstanceHealthCheck response = instanceApi.getInstanceHealthCheck(healthCheckInstance.getHeatStackId(), healthCheckInstance.getHealthCheckInstance());
                log.debug("Response for healthCheckInstance={}: {}", healthCheckInstance, response);
                String status = response.getStatus();
                if (!"RUNNING".equals(status.toUpperCase())) {
                    log.info("Poll status: {} for {}", status, healthCheckInstance);
                    instanceApi.deleteInstanceHealthCheck(healthCheckInstance.getHeatStackId(), healthCheckInstance.getHealthCheckInstance());
                    return new InstanceStatusTuple(healthCheckInstance.getHeatStackId(), status);
                }
                sleep(10_000L);
            }
            return new InstanceStatusTuple(healthCheckInstance.getHeatStackId(), "Timeout");
        }
    }

    private class InstanceStatusTuple {
        private final String healthCheckInstance;
        private final String status;

        InstanceStatusTuple(String healthCheckInstance, String status) {
            this.healthCheckInstance = healthCheckInstance;
            this.status = status;
        }

        String getHealthCheckInstance() {
            return healthCheckInstance;
        }

        String getStatus() {
            return status;
        }
    }

}
