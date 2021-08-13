package org.onap.so.adapters.cnf.service.aai;

import com.google.common.hash.Hashing;
import org.onap.so.adapters.cnf.model.instantiation.AaiUpdateRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
class AaiIdGeneratorService {

    String generateId(K8sRbInstanceResourceStatus resourceStatus, AaiUpdateRequest aaiUpdateRequest) {
        K8sRbInstanceGvk gvk = resourceStatus.getGvk();
        String originalString = resourceStatus.getName() + gvk.getKind() + gvk.getGroup() + gvk.getVersion() +
                aaiUpdateRequest.getInstanceId() + aaiUpdateRequest.getCloudOwner() +
                aaiUpdateRequest.getCloudRegion() + aaiUpdateRequest.getTenantId();

        return Hashing.sha256()
                .hashString(originalString, StandardCharsets.UTF_8)
                .toString();
    }

}
