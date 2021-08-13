package org.onap.so.adapters.cnf.service.aai;

import com.google.common.hash.Hashing;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
class AaiIdGeneratorService {


    // ADD NAMESPACE
    // ADD SELFLINK
    String generateId(K8sRbInstanceResourceStatus resourceStatus) {
        K8sRbInstanceGvk gvk = resourceStatus.getGvk();
        String originalString = resourceStatus.getName() + gvk.getKind() + gvk.getGroup() + gvk.getVersion();

        return Hashing.sha256()
                .hashString(originalString, StandardCharsets.UTF_8)
                .toString();
    }

}
