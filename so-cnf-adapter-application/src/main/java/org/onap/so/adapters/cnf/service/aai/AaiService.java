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

import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.adapters.cnf.AaiConfiguration;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.util.IAaiRepository;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AaiService {

    private final static Logger log = LoggerFactory.getLogger(AaiService.class);

    private final MulticloudClient multicloudClient;
    private final AaiResponseParser responseParser;
    private final AaiConfiguration configuration;

    public AaiService(MulticloudClient multicloudClient, AaiResponseParser responseParser, AaiConfiguration configuration) {
        this.multicloudClient = multicloudClient;
        this.responseParser = responseParser;
        this.configuration = configuration;
    }

    public void aaiUpdate(AaiRequest aaiRequest) throws BadResponseException, BulkProcessFailed {
        List<KubernetesResource> k8sResList = parseStatus(aaiRequest);
        IAaiRepository aaiRepository = IAaiRepository.instance(configuration.isEnabled());
        k8sResList.forEach(status -> aaiRepository.update(status, aaiRequest));
        aaiRepository.delete(aaiRequest, k8sResList);
        aaiRepository.commit(false);
    }

    public void aaiDelete(AaiRequest aaiRequest) throws BulkProcessFailed {
        String instanceID = aaiRequest.getInstanceId();
        boolean enabled = configuration.isEnabled();
        if (instanceID == null || instanceID.isEmpty() || instanceID.equals("null")) {
            //we skip deletion of resources instance that was not created properly and instance id was not stored in AAI
            log.warn("Undefined instance ID aai-delete attempt. Skipping aai-delete");
            enabled = false;
        }
        IAaiRepository aaiRepository = IAaiRepository.instance(enabled);
        aaiRepository.delete(aaiRequest, List.of());
        aaiRepository.commit(false);
    }

    private List<KubernetesResource> parseStatus(AaiRequest aaiRequest) throws BadResponseException {
        String instanceId = aaiRequest.getInstanceId();
        K8sRbInstanceStatus instanceStatus = multicloudClient.getInstanceStatus(instanceId);

        List<K8sRbInstanceResourceStatus> resourcesStatus = instanceStatus.getResourcesStatus();
        return resourcesStatus.stream()
                .map(status -> responseParser.parse(status, aaiRequest))
                .collect(Collectors.toList());
    }
}
