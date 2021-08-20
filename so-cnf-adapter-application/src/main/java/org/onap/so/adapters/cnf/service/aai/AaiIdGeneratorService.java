package org.onap.so.adapters.cnf.service.aai;

import com.google.common.hash.Hashing;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
class AaiIdGeneratorService {

    String generateId(K8sRbInstanceResourceStatus resourceStatus, AaiRequest aaiRequest) {
        K8sRbInstanceGvk gvk = resourceStatus.getGvk();
        String originalString = resourceStatus.getName() + gvk.getKind() + gvk.getGroup() + gvk.getVersion() +
                aaiRequest.getInstanceId() + aaiRequest.getCloudOwner() +
                aaiRequest.getCloudRegion() + aaiRequest.getTenantId();

        return Hashing.sha256()
                .hashString(originalString, StandardCharsets.UTF_8)
                .toString();
    }

}
