package org.onap.so.adapters.cnf.service.statuscheck;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SimpleStatusCheckService {

    private final Logger log = LoggerFactory.getLogger(SimpleStatusCheckService.class);
    private final MulticloudClient instanceApi;

    @Autowired
    public SimpleStatusCheckService(MulticloudClient instanceApi) {
        this.instanceApi = instanceApi;
    }

    public Map<String, String> getStatusCheck(String heatStackId) {
        log.info("SIMPLE STATUS CHECK - START");
        Map<String, String> podStatus = new HashMap<>();
        int checkCount = 30;
        while (checkCount > 0) {
            String name = "";
            String status = "";
            boolean continueCheck = false;
            K8sRbInstanceStatus instanceStatus = instanceApi.getInstanceStatus(heatStackId);
            log.debug("Get status for heatStackId={}", heatStackId);
            for (K8sRbInstanceResourceStatus it: instanceStatus.getResourcesStatus()) {
                K8sRbInstanceGvk gvk = it.getGvk();
                name = it.getName();
                String kind = gvk.getKind();
                String group = gvk.getGroup();
                String version = gvk.getVersion();
                log.debug("Resource: name={} kind={} group={} version={}", name, kind, group, version);
                if ("Pod".equalsIgnoreCase(kind)) {
                    if (!"".equals(group)) {
                        version = group + "/" + version;
                    }
                    Map podState = mapStatusToPodState(it.getStatus().get("status"));
                    status = podState.get("phase").toString();
                    if (!"Running".equalsIgnoreCase(status)) {
                        continueCheck = true;
                        log.info("Pod {} has INVALID state {}", name, status);
                    } else {
                        log.info("Pod {} has VALID state {}", name, status);
                    }
                }
            }
            if (continueCheck) {
                checkCount--;
                if (checkCount == 0) {
                    podStatus.put(name, "Timeout");
                }
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    log.error("Interrupted exception: ", e);
                }
            } else {
                checkCount = 0;
                podStatus.put(name, status);
            }
        }
        log.info("SIMPLE STATUS CHECK - END SUCCESS");

        return podStatus;
    }

    private Map mapStatusToPodState(Object status) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(status, Map.class);
    }

}
