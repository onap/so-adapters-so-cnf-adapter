package org.onap.so.adapters.cnf.service.statuscheck;

import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckInstanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleStatusCheckService {

    private final Logger log = LoggerFactory.getLogger(SimpleStatusCheckService.class);
    private final MulticloudClient instanceApi;

    @Autowired
    public SimpleStatusCheckService(MulticloudClient instanceApi) {
        this.instanceApi = instanceApi;
    }

    public StatusCheckInstanceResponse getStatusCheck(String instanceId) {
        log.info("SIMPLE STATUS CHECK - START");
        int checkCount = 30;
        StatusCheckInstanceResponse result = null;
        while (checkCount > 0) {
            K8sRbInstanceStatus instanceStatus = instanceApi.getInstanceStatus(instanceId);
            boolean isInstanceReady = instanceStatus.isReady();
            log.info("Get status for instanceId={}", instanceId);
            log.info("Instance status={}", instanceStatus);

            if (isInstanceReady) {
                result = new StatusCheckInstanceResponse(instanceId, null, true);
            } else {
                checkCount--;
                if (checkCount == 0) {
                    result = new StatusCheckInstanceResponse(instanceId, null, false);
                }
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    log.error("Interrupted exception: ", e);
                }
            }
        }
        log.info("SIMPLE STATUS CHECK - END SUCCESS");

        return result;
    }

}
