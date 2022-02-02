/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
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
package org.onap.so.adapters.cnf.service.upgrade;

import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.upgrade.InstanceUpgradeRequest;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceUpgradeService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceUpgradeService.class);

    private final MulticloudClient instanceApi;

    public InstanceUpgradeService(MulticloudClient multicloudClient) {
        this.instanceApi = multicloudClient;
    }

    public String upgradeInstance(String instanceId, InstanceUpgradeRequest upgradeRequest) throws BadResponseException {
        logger.debug("UpgradeService upgradeInstance for instanceId: {}- START", instanceId);
        MulticloudInstanceRequest multicloudInstanceRequest = new MulticloudInstanceRequest();
        if (upgradeRequest.getK8sRBProfileName() != null) {
            multicloudInstanceRequest.setRbName(upgradeRequest.getModelInvariantId());
            multicloudInstanceRequest.setRbVersion(upgradeRequest.getModelCustomizationId());
            multicloudInstanceRequest.setProfileName(upgradeRequest.getK8sRBProfileName());
            multicloudInstanceRequest.setCloudRegion(upgradeRequest.getCloudRegionId());
            multicloudInstanceRequest.setLabels(upgradeRequest.getLabels());
            multicloudInstanceRequest.setOverrideValues(upgradeRequest.getOverrideValues());
        } else {
            throw new NullPointerException("k8sProfileName should not be null");
        }
        logger.info("Upgrade request: {}", multicloudInstanceRequest);
        logger.debug("UpgradeService upgradeInstance- END");
        return instanceApi.upgradeInstance(instanceId, multicloudInstanceRequest);
    }
}
