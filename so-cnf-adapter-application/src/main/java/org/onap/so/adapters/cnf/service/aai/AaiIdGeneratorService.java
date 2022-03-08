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

package org.onap.so.adapters.cnf.service.aai;

import com.google.common.hash.Hashing;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
class AaiIdGeneratorService {

    String generateId(K8sRbInstanceResourceStatus resourceStatus, AaiRequest aaiRequest) {
        K8sRbInstanceGvk gvk = resourceStatus.getGvk();
        K8sStatusMetadata metadata = resourceStatus.getStatus().getK8sStatusMetadata();
        String originalString = aaiRequest.getInstanceId() + resourceStatus.getName() +
                (metadata.getNamespace() != null ? metadata.getNamespace() : "") +
                gvk.getKind() + gvk.getGroup() + gvk.getVersion() +
                aaiRequest.getCloudOwner() + aaiRequest.getCloudRegion() + aaiRequest.getTenantId();

        return Hashing.sha256()
                .hashString(originalString, StandardCharsets.UTF_8)
                .toString();
    }

}
